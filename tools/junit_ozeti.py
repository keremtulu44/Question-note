"""Small CI-only JUnit parser; variant counts stay separate (shared tests are not summed twice)."""
from pathlib import Path
import argparse
import sys
from xml.etree import ElementTree

parser = argparse.ArgumentParser()
parser.add_argument("mode", choices=("summary", "failures"))
parser.add_argument("directories", nargs="+")
args = parser.parse_args()
totals = dict(tests=0, failures=0, errors=0, skipped=0)
details = []
files = [p for directory in args.directories for p in Path(directory).glob("TEST-*.xml")]
for path in sorted(files):
    root = ElementTree.parse(path).getroot()
    for suite in root.iter("testsuite"):
        for key in totals:
            totals[key] += int(suite.attrib.get(key, "0"))
    for case in root.iter("testcase"):
        for failure in case:
            if failure.tag in ("failure", "error"):
                message = failure.attrib.get("message", "").replace("\n", " ")
                details.append(
                    f"{path.parent.name}: {case.attrib.get('classname', '')} > {case.attrib.get('name', '')}: "
                    f"{failure.attrib.get('type', failure.tag)}: {message}\n{(failure.text or '').strip()[:3500]}"
                )
if args.mode == "failures":
    print("\n\n".join(details) if details else "Başarısız JUnit testi yok.")
    sys.exit(0)
if not files:
    print("JUnit XML raporu yok (0 test)")
    sys.exit(1)
print(f"{totals['tests']} test; {totals['failures']} başarısız; {totals['errors']} hata; {totals['skipped']} atlandı")
sys.exit(1 if not totals["tests"] or totals["failures"] or totals["errors"] else 0)
