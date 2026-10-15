// readme-banner.html 을 25fps 로 7초 캡처해 ../readme-banner.png(APNG, 1684x528, 배경 투명)로 묶는다.
// 필요한 것: Node 의 playwright(Chromium), ffmpeg, 한글 글꼴(Noto Sans CJK KR).
// 실행: node docs/images/source/render-readme-banner.js
const { chromium } = require('playwright');
const { execFileSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const FPS = 25;
const TOTAL_MS = 7000;

(async () => {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'readme-banner-'));
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 842, height: 264 }, deviceScaleFactor: 2 });
  await page.goto('file://' + path.join(__dirname, 'readme-banner.html'));
  await page.evaluate(() => document.fonts.ready);
  for (let i = 0; i < TOTAL_MS * FPS / 1000; i++) {
    await page.evaluate(t => window.render(t), i * 1000 / FPS);
    await page.screenshot({ path: path.join(dir, `f${String(i).padStart(4, '0')}.png`), omitBackground: true });
  }
  await browser.close();
  execFileSync('ffmpeg', ['-v', 'error', '-y', '-framerate', String(FPS), '-i', path.join(dir, 'f%04d.png'),
    '-plays', '0', '-f', 'apng', path.join(__dirname, '..', 'readme-banner.png')], { stdio: 'inherit' });
  fs.rmSync(dir, { recursive: true });
})();
