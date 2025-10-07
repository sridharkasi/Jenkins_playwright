from playwright.sync_api import sync_playwright
from sentence_transformers import SentenceTransformer, util
import os
import re
import pandas as pd
import configparser

def test_index_shows_text():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True, args=["--no-sandbox"])
        page = browser.new_page()
        page.goto("http://127.0.0.1:5000")
        content = page.content()
        assert "Hello from Jenkins demo" in content
        browser.close()

if __name__ == "__main__":
    test_index_shows_text()
    print("OK")
