## 概述

1. 用户可以申请创建、申请加入、浏览，进入，搜索商店
2. 商店内可以查看商品
3. 商店应该包含的属性：
   1. 商店id
   2. 名称
   3. 店主id
   4. 图标url
   5. 描述
   6. 评分
   7. 有效位
4. 供参考的用户库表如下：

```sql
CREATE TABLE shops(
    shop_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '商店ID',
    name VARCHAR(100) NOT NULL COMMENT '商店名称，不允许为空',
    owner_id INT NOT NULL COMMENT '店主ID，不允许为空',
    icon_url VARCHAR(255) COMMENT '商店图标链接'
    description VARCHAR(255) COMMENT '商店描述',
    rating DECIMAL(3, 2) COMMENT '商店评分，保留两位小数'
    is_valid TINYINT(1) NOT NULL DEFAULT 0 COMMENT '商店是否有效，
    1表示有效，0表示无效，默认为0'
) COMMENT='商店表';
```

## 后端接口

#### 1.获取商店列表

##### 请求方式

- **方法**: `GET`
- **路径**: `/api/shop/all`
- **Headers:** `token: xxx`

##### 请求参数

无

##### 返回示例

```json
{
    "code": "200",
    "msg": null,
    "data": [
    {
        "shop_id": "123",
        "name": "123",
        "owner_id": "123",
        "icon_url": "xxx",
        "description": "123",
        "rating": 123.45,
        "is_valid": 1,
    },
    ...
    ]
}
```

#### 2.创建商店

##### 请求方式

- **方法**: POST
- **路径**: `/api/shop/create`
- **Headers:** `token: xxx`

##### **请求参数**

**类型**: `JSON body`

| 参数名      | 类型   | 是否必填 |
| ----------- | ------ | -------- |
| name        | String | 是       |
| owner_id    | String | 是       |
| icon_url    | String | 否       |
| description | String | 否       |

##### 返回示例

```JSON
{
    "code": "200",
    "msg": null,
    "data": "创建成功"
}
{
    "code": 400,
    "data": null,
    "msg": "创建失败"
}
```

#### 3.进入商店（查看商店详情）

##### 请求方式

**方法**: `POST`

**路径**: `/api/shop/detail/{shop_id}`

##### 请求参数

shop_id

##### 返回示例

```json
{
    "code": "200",
    "msg": null,
    "data": [
    {
      "id": "101",
      "title": "深入理解Java虚拟机",
      "price": 99.50,
      "rate": 9.5,
      "description": "Java开发者必读经典，全面讲解JVM工作原理",
      "cover": "https://example.com/covers/jvm.jpg",
      "detail": "本书详细讲解了Java虚拟机的体系结构、内存管理、字节码执行等核心内容",
      "specifications": [
        {
          "id": "1001",
          "item": "作者",
          "value": "周志明",
          "productId": "101"
        },
        {
          "id": "1002",
          "item": "副标题",
          "value": "JVM高级特性与最佳实践",
          "productId": "101"
        },
        {
          "id": "1003",
          "item": "ISBN",
          "value": "9787111421900",
          "productId": "101"
        },
        {
          "id": "1004",
          "item": "装帧",
          "value": "平装",
          "productId": "101"
        },
        {
          "id": "1005",
          "item": "页数",
          "value": "540",
          "productId": "101"
        },
        {
          "id": "1006",
          "item": "出版社",
          "value": "机械工业出版社",
          "productId": "101"
        },
        {
          "id": "1007",
          "item": "出版日期",
          "value": "2013-09-01",
          "productId": "101"
        }
      ]
    },
    {
      "id": "102",
      "title": "代码整洁之道",
      "price": 59.00,
      "rate": 9.2,
      "description": "软件工程领域的经典著作",
      "cover": "https://example.com/covers/clean-code.jpg",
      "detail": "本书提出一种观念：代码质量与其整洁度成正比",
      "specifications": [
        {
          "id": "1008",
          "item": "作者",
          "value": "Robert C. Martin",
          "productId": "102"
        },
        {
          "id": "1009",
          "item": "副标题",
          "value": "程序员的职业素养",
          "productId": "102"
        },
        {
          "id": "1010",
          "item": "ISBN",
          "value": "9787121316633",
          "productId": "102"
        },
        {
          "id": "1011",
          "item": "装帧",
          "value": "精装",
          "productId": "102"
        },
        {
          "id": "1012",
          "item": "页数",
          "value": "388",
          "productId": "102"
        },
        {
          "id": "1013",
          "item": "出版社",
          "value": "人民邮电出版社",
          "productId": "102"
        },
        {
          "id": "1014",
          "item": "出版日期",
          "value": "2018-01-01",
          "productId": "102"
        }
      ]
    }
  ],
}
{
    "code": "400",
    "data": null,
    "msg": "获取商品信息失败"
}
```

#### 4.更新商店信息

##### 请求方式

- **方法**: `PUT`
- **路径**: `/api/shop/update`
- **Headers:** `token: xxx`

##### 请求参数

- **类型**: `JSON body`

| 参数名      | 类型   | 是否必填 |
| ----------- | ------ | -------- |
| name        | String | 否       |
| owner_id    | String | 否       |
| icon_url    | String | 否       |
| description | String | 否       |

##### 返回示例

```json
{
    "code": "200",
    "msg": null,
    "data": "更新成功"
}
```

#### 5. 删除商店

##### 请求方式

- **方法**: `DELETE`
- **路径**: `/api/shop/delete`
- **Headers:** `token: xxx`

##### 请求参数

无

##### 返回示例

```json
{
    "code": "200",
    "msg": null,
    "data": "删除成功"
}
```