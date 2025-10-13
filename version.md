# 0.0.10
### 更新时间
2025/10/13

### 更新内容：
1. js解析支持匿名函数功能
2. 增加函数IReactProjectOperator#parseRouteDefines，支持解析路由定义（仅支持.js文件路由定义）


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