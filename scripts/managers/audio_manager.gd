extends Node

## Gerenciador de Áudio com Efeitos Sonoros Sintetizados Proceduralmente
## Funciona 100% offline sem necessidade de arquivos externos adicionais.

var sfx_volume: float = 0.8
var music_volume: float = 0.8

var sfx_players: Array[AudioStreamPlayer] = []
const POOL_SIZE: int = 8
var current_player_idx: int = 0

var cached_samples: Dictionary = {}

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_setup_audio_players()
	_pregenerate_sounds()

func _setup_audio_players() -> void:
	for i in range(POOL_SIZE):
		var p := AudioStreamPlayer.new()
		p.name = "SFXPlayer_%d" % i
		p.bus = "Master"
		add_child(p)
		sfx_players.append(p)

func _pregenerate_sounds() -> void:
	cached_samples["click"] = _generate_tone(880.0, 0.05, 0.3, "sine")
	cached_samples["coin"] = _generate_arp([987.0, 1318.0], 0.12, 0.4)
	cached_samples["magic"] = _generate_tone(440.0, 0.18, 0.4, "sine", 220.0)
	cached_samples["arrow"] = _generate_noise(0.12, 0.35)
	cached_samples["explosion"] = _generate_noise(0.35, 0.5, true)
	cached_samples["slow"] = _generate_tone(220.0, 0.3, 0.3, "saw", 110.0)
	cached_samples["piercing"] = _generate_tone(660.0, 0.25, 0.4, "square", 880.0)
	cached_samples["upgrade"] = _generate_arp([523.0, 659.0, 784.0, 1046.0], 0.25, 0.4)
	cached_samples["victory"] = _generate_arp([440.0, 554.0, 659.0, 880.0], 0.4, 0.5)
	cached_samples["defeat"] = _generate_arp([440.0, 415.0, 392.0, 349.0], 0.4, 0.5)
	cached_samples["boss_roar"] = _generate_tone(110.0, 0.6, 0.6, "saw", 55.0)
	cached_samples["meteor"] = _generate_noise(0.45, 0.6, true)
	cached_samples["flame"] = _generate_noise(0.3, 0.4, false)
	cached_samples["mine_explosion"] = _generate_noise(0.4, 0.55, true)
	cached_samples["turret_shot"] = _generate_tone(550.0, 0.08, 0.35, "square")
	cached_samples["sniper_shot"] = _generate_tone(880.0, 0.22, 0.5, "saw", 220.0)
	cached_samples["shield_up"] = _generate_arp([330.0, 440.0, 660.0], 0.25, 0.45)

func play_sfx(sound_name: String) -> void:
	if not cached_samples.has(sound_name):
		return
		
	var player: AudioStreamPlayer = sfx_players[current_player_idx]
	current_player_idx = (current_player_idx + 1) % POOL_SIZE
	
	player.stream = cached_samples[sound_name]
	player.volume_db = linear_to_db(sfx_volume)
	player.play()

# Geradores procedurais de AudioStreamWAV em formato PCM 16-bit Mono (44100Hz)
func _generate_tone(freq: float, duration: float, volume: float = 0.5, wave_type: String = "sine", end_freq: float = -1.0) -> AudioStreamWAV:
	var sample_rate := 44100
	var total_frames := int(sample_rate * duration)
	var byte_array := PackedByteArray()
	byte_array.resize(total_frames * 2)
	
	var actual_end_freq := end_freq if end_freq > 0.0 else freq
	var phase := 0.0
	
	for i in range(total_frames):
		var t := float(i) / float(sample_rate)
		var progress := float(i) / float(total_frames)
		var current_freq := lerpf(freq, actual_end_freq, progress)
		phase += (TAU * current_freq) / float(sample_rate)
		
		var sample_val := 0.0
		match wave_type:
			"sine":
				sample_val = sin(phase)
			"square":
				sample_val = 1.0 if sin(phase) >= 0.0 else -1.0
			"saw":
				sample_val = (fmod(phase, TAU) / PI) - 1.0
				
		# Envelope simples (fade out)
		var envelope := 1.0 - progress
		var int_val := int(sample_val * envelope * volume * 32767.0)
		int_val = clampi(int_val, -32768, 32767)
		
		byte_array.encode_s16(i * 2, int_val)
		
	var wav := AudioStreamWAV.new()
	wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.mix_rate = sample_rate
	wav.stereo = false
	wav.data = byte_array
	return wav

func _generate_arp(notes: Array, duration: float, volume: float = 0.5) -> AudioStreamWAV:
	var sample_rate := 44100
	var total_frames := int(sample_rate * duration)
	var byte_array := PackedByteArray()
	byte_array.resize(total_frames * 2)
	
	var note_duration := total_frames / notes.size()
	var phase := 0.0
	
	for i in range(total_frames):
		var note_idx := clampi(int(i / note_duration), 0, notes.size() - 1)
		var freq: float = notes[note_idx]
		phase += (TAU * freq) / float(sample_rate)
		
		var sample_val := sin(phase)
		var note_progress := float(i % note_duration) / float(note_duration)
		var envelope := (1.0 - note_progress * 0.8) * volume
		
		var int_val := int(sample_val * envelope * 32767.0)
		int_val = clampi(int_val, -32768, 32767)
		byte_array.encode_s16(i * 2, int_val)
		
	var wav := AudioStreamWAV.new()
	wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.mix_rate = sample_rate
	wav.stereo = false
	wav.data = byte_array
	return wav

func _generate_noise(duration: float, volume: float = 0.5, lowpass: bool = false) -> AudioStreamWAV:
	var sample_rate := 44100
	var total_frames := int(sample_rate * duration)
	var byte_array := PackedByteArray()
	byte_array.resize(total_frames * 2)
	
	var last_val := 0.0
	for i in range(total_frames):
		var progress := float(i) / float(total_frames)
		var raw_val := (randf() * 2.0) - 1.0
		
		if lowpass:
			last_val = lerpf(last_val, raw_val, 0.15)
			raw_val = last_val
			
		var envelope := (1.0 - progress) * volume
		var int_val := int(raw_val * envelope * 32767.0)
		int_val = clampi(int_val, -32768, 32767)
		byte_array.encode_s16(i * 2, int_val)
		
	var wav := AudioStreamWAV.new()
	wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.mix_rate = sample_rate
	wav.stereo = false
	wav.data = byte_array
	return wav
