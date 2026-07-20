from playwright.sync_api import sync_playwright
from pathlib import Path
import sys

html_path = Path(__file__).parent.parent / "docs" / "architecture" / "DIAGRAMA.html"
if not html_path.exists():
    print(f"NOT FOUND: {html_path}", file=sys.stderr)
    sys.exit(1)

url = html_path.as_uri()

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    ctx = browser.new_context(viewport={"width": 1440, "height": 900})
    page = ctx.new_page()

    console_msgs = []
    page.on("console", lambda m: console_msgs.append(f"[{m.type}] {m.text}"))
    page.on("pageerror", lambda e: console_msgs.append(f"[pageerror] {e}"))

    page.goto(url)
    page.wait_for_load_state("networkidle")
    page.wait_for_timeout(3000)  # give Mermaid time to render

    out = Path(__file__).parent / "_out"
    out.mkdir(exist_ok=True)

    page.screenshot(path=str(out / "diagrama_full.png"), full_page=True)

    # Also screenshot each Mermaid diagram individually
    mermaids = page.locator(".mermaid").all()
    for i, m in enumerate(mermaids, start=1):
        try:
            m.screenshot(path=str(out / f"mermaid_{i}.png"))
        except Exception as e:
            print(f"failed to screenshot mermaid {i}: {e}", file=sys.stderr)

    # Report SVG rendering status
    svg_count = page.locator(".mermaid svg").count()
    print(f"MERMAID_SVG_COUNT={svg_count}")

    for msg in console_msgs:
        print(msg)

    browser.close()
