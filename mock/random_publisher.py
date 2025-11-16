#!/usr/bin/env python3
import argparse
import logging
import random
import shlex
import signal
import subprocess
import sys
import time


def load_commands(path):
    with open(path, "r", encoding="utf-8") as f:
        lines = [l.strip() for l in f]
    cmds = [l for l in lines if l and not l.startswith("#")]
    return cmds


def run_once(cmd, dry_run=False):
    logging.info("Selected command: %s", cmd)
    if dry_run:
        print(cmd)
        return 0
    args = shlex.split(cmd)
    try:
        proc = subprocess.run(args)
        return proc.returncode
    except FileNotFoundError:
        logging.error("Command not found: %s", args[0])
        return 127
    except Exception as e:
        logging.exception("Error running command: %s", e)
        return 1


def main():
    parser = argparse.ArgumentParser(description="Run random commands from a file in a loop.")
    parser.add_argument("-f", "--file", default="cmd.txt", help="File containing commands (one per line)")
    parser.add_argument("-i", "--interval", type=float, default=None, help="Fixed seconds between commands (overrides min/max)")
    parser.add_argument("--min-interval", type=float, default=1.0, help="Minimum interval in seconds for random delay")
    parser.add_argument("--max-interval", type=float, default=15.0, help="Maximum interval in seconds for random delay")
    parser.add_argument("--dry-run", action="store_true", help="Don't execute, just print chosen commands")
    parser.add_argument("--once", action="store_true", help="Run exactly one random command and exit")
    parser.add_argument("--seed", type=int, default=None, help="Optional random seed for reproducibility")
    args = parser.parse_args()

    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s: %(message)s")

    if args.seed is not None:
        random.seed(args.seed)

    try:
        cmds = load_commands(args.file)
    except FileNotFoundError:
        logging.error("Commands file not found: %s", args.file)
        sys.exit(2)

    if not cmds:
        logging.error("No commands found in %s", args.file)
        sys.exit(2)

    stop = False

    def _signal_handler(sig, frame):
        nonlocal stop
        logging.info("Signal received, stopping after current iteration...")
        stop = True

    signal.signal(signal.SIGINT, _signal_handler)
    signal.signal(signal.SIGTERM, _signal_handler)

    if args.once:
        cmd = random.choice(cmds)
        rc = run_once(cmd, dry_run=args.dry_run)
        sys.exit(rc)

    if args.interval is not None:
        logging.info("Starting loop: choosing a random command every %s seconds (fixed)", args.interval)
    else:
        logging.info("Starting loop: choosing a random command with interval between %s and %s seconds", args.min_interval, args.max_interval)

    if args.min_interval > args.max_interval:
        logging.error("--min-interval must be <= --max-interval")
        sys.exit(2)

    while not stop:
        cmd = random.choice(cmds)
        run_once(cmd, dry_run=args.dry_run)
        # determine next sleep interval
        if args.interval is not None:
            sleep_interval = float(args.interval)
        else:
            sleep_interval = random.uniform(float(args.min_interval), float(args.max_interval))

        slept = 0.0
        while slept < sleep_interval and not stop:
            to_sleep = min(0.5, sleep_interval - slept)
            time.sleep(to_sleep)
            slept += to_sleep


if __name__ == "__main__":
    main()


# python3 random_publisher.py -f cmd.txt
# python3 random_publisher.py -f cmd.txt -i 10
# python3 random_publisher.py -f cmd.txt --min-interval 2 --max-interval 5