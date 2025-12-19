package com.xycm.poc.webkit.config;

import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomWebViewClient extends WebViewClient {

    private boolean debugInjected = false;

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    /**
     * 页面加载完成后执行
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // 已注入，直接返回
        if (debugInjected) {
            return;
        }
        debugInjected = true;
        injectDebugButton(view);
    }

    private void injectDebugButton(WebView view) {
        String jsCode = "setTimeout(function() {" +
                "  console.log('🔧 开始注入Android调试按钮');" +
                "  " +
                "  // 移除可能存在的旧按钮" +
                "  var oldBtn = document.getElementById('android-debug-btn');" +
                "  if (oldBtn) oldBtn.remove();" +
                "  " +
                "  // 创建按钮" +
                "  var btn = document.createElement('div');" +
                "  btn.id = 'android-debug-btn';" +
                "  btn.innerHTML = '🐛';" +
                "  " +
                "  // 设置样式 - 确保可见" +
                "  btn.style.position = 'fixed';" +
                "  btn.style.bottom = '120px';" +
                "  btn.style.right = '20px';" +
                "  btn.style.width = '60px';" +
                "  btn.style.height = '60px';" +
                "  btn.style.backgroundColor = 'red';" +
                "  btn.style.color = 'white';" +
                "  btn.style.borderRadius = '30px';" +
                "  btn.style.display = 'flex';" +
                "  btn.style.alignItems = 'center';" +
                "  btn.style.justifyContent = 'center';" +
                "  btn.style.fontSize = '28px';" +
                "  btn.style.cursor = 'pointer';" +
                "  btn.style.zIndex = '999999';" +
                "  btn.style.boxShadow = '0 4px 12px rgba(255,0,0,0.8)';" +
                "  " +
                "  // 点击事件" +
                "  btn.onclick = function() {" +
                "    console.log('🎯 Android调试按钮被点击');" +
                "    " +
                "    // 1. 尝试显示现有的vConsole" +
                "    if (window.vConsole && window.vConsole.show) {" +
                "      window.vConsole.show();" +
                "      return;" +
                "    }" +
                "    " +
                "    // 2. 尝试加载vConsole" +
                "    if (typeof VConsole !== 'undefined') {" +
                "      window.vConsole = new VConsole();" +
                "      window.vConsole.show();" +
                "    } else {" +
                "      // 3. 从CDN加载" +
                "      var script = document.createElement('script');" +
                "      script.src = 'https://cdn.jsdelivr.net/npm/vconsole@latest/dist/vconsole.min.js';" +
                "      script.onload = function() {" +
                "        if (typeof VConsole !== 'undefined') {" +
                "          window.vConsole = new VConsole();" +
                "          window.vConsole.show();" +
                "        }" +
                "      };" +
                "      document.head.appendChild(script);" +
                "    }" +
                "  };" +
                "  " +
                "  // 添加到页面" +
                "  document.body.appendChild(btn);" +
                "  console.log('Android调试按钮注入成功');" +
                "  " +
                "  // 测试按钮是否真的添加了" +
                "  console.log('按钮元素:', btn);" +
                "  console.log('按钮是否在DOM中:', document.body.contains(btn));" +
                "  console.log('按钮可见性:', btn.offsetParent !== null);" +
                "}, 1000);";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(jsCode, null);
        }
    }
}
