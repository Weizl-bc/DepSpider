# 0.0.9
### 更新时间
2025/9/18

### 更新内容：
1. 增加单个js文件解析到变量的功能
> 例如，支持解析：
```javascript
{
    path: '/gpsmap',
    lazy: () => import('MicroSiteBee/gpsmap'),
    title: '',
    hideNav: platform === 'app',
},
```