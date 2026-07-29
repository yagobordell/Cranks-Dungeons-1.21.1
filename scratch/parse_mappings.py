import sys

in_class = False
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    for line in f:
        line = line.strip()
        if line.startswith('c\t'):
            if 'net/minecraft/entity/LivingEntity' in line or 'class_1309' in line:
                in_class = True
            else:
                in_class = False
        elif in_class and line.startswith('m\t'):
            print(line)
