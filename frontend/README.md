# WOS 前端

## 介绍
这是上海交通大学图书馆WOS学术资源平台的前端代码部分。
前端使用JavaScript，CSS，HTML，使用React作为框架。
该部分包含了所有的前端代码。

目前主要的开发者：
- 陈家树

## 运行代码

- **软件需求**
    确保计算机中下载了Node.js。

- **Package下载**
    使用以下命令下载Packages：
    ```shell
    npm install
    ```
    如果你无法下载，尝试先使用以下命令：
    ```shell
    npm config set legacy-peer-deps true
    ```
    然后再次尝试。

- **运行代码**
    使用以下命令来运行前端代码：
    ```shell
    npm run start
    ```
    ***注意：以上命令请在frontEnd文件下运行而不是wos_project***

- **前端代码测试**
    暂无

## 文件夹与文件介绍
以下是对根目录下的文件夹以及文件的解释。
**1.`components/`**:储存页面以及组件的js文件
- `pages/` 储存了目前所有的的网页页面代码
    - `generalSearch.js` 是基础全文搜索的页面
    - `advancedSearch.js` 是高级检索的页面
    - `searchDetail.js` 是展示搜索结果的页面
    - `paperDetail.js` 是展示论文具体信息的页面
    - `disciplinaryAnalysis.js` 是用于学科发展分析的页面

- `header.js` 是页眉组件
- `footer.js` 是页脚组件
- `conditionRow.js` 是高级检索中的需要的条件组件，被使用在 `searchInput.js`中
- `searchInput.js` 是高级检索页面需要的搜索组件
- `fulltextSearch.js` 是全文搜索的搜索组件
- `paperCard.js` 是 `searchDetail`中使用的论文展示组件
- `detailCard.js` 是 `paperDetail`中使用的论文详细信息展示组件

**2. `images/`** 储存前端设计时使用的图片。

**3. `stores/`** 储存的是Redux相关文件，但是目前**未使用**。

**4. `styles/`** 储存所有的css文件，命名与对应的js文件相同。

**5. `App.js`** 是Route目录。

**6. `index.js`** 是启动文件。