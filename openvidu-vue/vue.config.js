const fs = require('fs');
const path = require('path');

module.exports = {
  devServer: {
    https: {
      key: fs.readFileSync(path.resolve(__dirname, 'key.pem')),
      cert: fs.readFileSync(path.resolve(__dirname, 'cert.pem')),
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8443/live/api', // Spring Boot 서버 주소
        changeOrigin: true,
        secure: false, // 이 옵션을 사용하면 인증서가 신뢰되지 않아도 요청을 보냅니다.
        pathRewrite: { '^/api': '' } // '/api' 경로를 ''로 변경하여 '/live/api/**'로 전달됩니다.
      }
    }
  }
};
