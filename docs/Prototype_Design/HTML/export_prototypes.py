"""
将所有原型 HTML 页面截图并合成为一个 PDF 文件。
用法: python export_prototypes.py
输出: prototypes.pdf
"""

import os
from pathlib import Path
from playwright.sync_api import sync_playwright
from PIL import Image

# 页面顺序：Login -> TA系列 -> MO系列 -> Admin
PAGES = [
    ("login.html", "登录 / 注册"),
    ("ta_dashboard.html", "应聘者 - 控制台"),
    ("ta_profile.html", "应聘者 - 个人档案"),
    ("ta_positions.html", "应聘者 - 岗位列表"),
    ("mo_dashboard.html", "课程组织者 - 控制台"),
    ("mo_post_position.html", "课程组织者 - 发布岗位"),
    ("mo_applicants.html", "课程组织者 - 应聘者列表"),
    ("admin_dashboard.html", "管理员 - 控制台"),
]

BASE_DIR = Path(__file__).parent
OUTPUT_PDF = BASE_DIR / "prototypes_new.pdf"
VIEWPORT_WIDTH = 1280


def main():
    images = []

    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page(viewport={"width": VIEWPORT_WIDTH, "height": 800})

        for filename, label in PAGES:
            filepath = BASE_DIR / filename
            if not filepath.exists():
                print(f"  跳过 {filename}（文件不存在）")
                continue

            url = filepath.as_uri()
            page.goto(url)
            page.wait_for_load_state("networkidle")

            screenshot_bytes = page.screenshot(full_page=True)
            tmp_path = BASE_DIR / f"_tmp_{filename}.png"
            tmp_path.write_bytes(screenshot_bytes)

            img = Image.open(tmp_path).convert("RGB")
            images.append(img)
            print(f"  截图完成: {label} ({filename})")

            tmp_path.unlink()

        browser.close()

    if not images:
        print("没有可导出的页面。")
        return

    # 合成 PDF，每页一张截图
    images[0].save(OUTPUT_PDF, save_all=True, append_images=images[1:], resolution=150)
    print(f"\n已导出: {OUTPUT_PDF}")


if __name__ == "__main__":
    main()
