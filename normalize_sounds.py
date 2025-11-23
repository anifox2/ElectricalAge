import json
import os

file_path = 'src/main/resources/assets/eln/sounds.json'

with open(file_path, 'r') as f:
    data = json.load(f)

new_data = {}
for key, value in data.items():
    new_key = key.lower()
    if 'sounds' in value:
        new_sounds = []
        for sound in value['sounds']:
            if isinstance(sound, str):
                new_sounds.append(sound.lower())
            elif isinstance(sound, dict):
                sound['name'] = sound['name'].lower()
                new_sounds.append(sound)
        value['sounds'] = new_sounds
    new_data[new_key] = value

with open(file_path, 'w') as f:
    json.dump(new_data, f, indent=4)
