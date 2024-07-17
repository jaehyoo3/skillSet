const fs = require('fs');
const path = require('path');

module.exports = {
  devServer: {
    port: 8238, // 포트 번호 지정
    server: {
      type: 'https',
      options: {
        key: fs.readFileSync(path.resolve(__dirname, 'key.pem')),
        cert: fs.readFileSync(path.resolve(__dirname, 'cert.pem')),
      },
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8443/live/api',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/api': '' }
      }
    }
  }
};