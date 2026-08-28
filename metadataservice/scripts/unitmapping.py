#!/usr/bin/env python3

import sys, os, subprocess as sp
import re
import json

import argparse

WHITELIST_FILE_EXTS = ( '.kt', '.cpp', '.cxx', 'c', '.h', '.hpp', '.hxx', '.java', '.sh', '.lua' )

def read_rules():
    rule_set = []

    for root, dirs, files in os.walk(os.getcwd()):
        for n in files:
            path = os.path.join(root, n)

            if n != 'unitmapping.txt' and n != 'unitmapping.json':
                continue

            with open(path) as f:
                rules = json.load(f)
                for i in rules:
                    e = {}
                    e["unit"] = i["unit"]

                    if isinstance(i["pattern"], list):
                        e["patterns"] = i["pattern"]
                    else:
                        e["patterns"] = [ i["pattern"] ]

                    rule_set.append(e)
    return rule_set

def get_fileset(folder_name):
    file_set = []
    for root, dirs, files in os.walk(folder_name):
        # Strip leading folder_name/...
        root = root[len(folder_name)+1:]
        for n in files:
            if n.endswith(WHITELIST_FILE_EXTS):
                path = os.path.join(root, n)
                path = path.replace(os.sep, '/') #handle windows directory separator
                file_set.append(path)
    return file_set

def match_files(file_set, rule_set):
    matched = []
    unmatched = []
    for f in file_set:
        was_matched = False
        for r in rule_set:
            for p in r["patterns"]:
                if re.match(p, f):
                    was_matched = True
                    matched.append((f, r["unit"]))
        if not was_matched:
            unmatched.append(f)
    return matched, unmatched

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='unitmapping command help')
    parser.add_argument('FILES', nargs='*', help="If given, print only information regarding these files.")

    parser.add_argument('--show-matched', action='store_true', default=False, help="Show matched files")
    parser.add_argument('--show-unmatched', action='store_true', default=False, help="Show unmatched files")
    parser.add_argument('--machine-output', action='store_true', default=False, help="Use machine parseable output")
    parser.add_argument('--failure-on-unmatched', action='store_true', default=False, help="Exit with failure if there are unmatched files")

    args = parser.parse_args(sys.argv[1:])

    if args.show_unmatched == False and args.show_matched == False:
        args.failure_on_unmatched = True

    rule_set = read_rules()

    if args.FILES:
        file_set = args.FILES
    else:
        file_set = get_fileset(os.getcwd())

    matched, unmatched = match_files(file_set, rule_set)

    if args.show_unmatched:
        if len(unmatched) > 0:
            if not args.machine_output:
                print("Unmatched files:")
                for i in unmatched:
                    print("\t", i)
            else:
                for i in unmatched:
                    print("not_matched:", os.path.join(os.getcwd(), i))

    if args.show_matched:
        for i in matched:
            if not args.machine_output:
                print(i[1], i[0])
            else:
                print(i[1] + ":", os.path.join(os.getcwd(), i[0]))

    if args.failure_on_unmatched and len(unmatched) > 0:
        print("{} unmatched files".format(len(unmatched)))
        sys.exit(1)
