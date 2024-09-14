# How to write document

### Before your start
Learn more from https://docusaurus.io/ .

#### Install nodejs
Make sure node.js 18.20.4 or above is installed.

Check version
```shell
node -v
```

If node.js version less than 18.20.4, can use `n` to install latest stable node.js
```shell
sudo npm cache clean -f
sudo npm install -g n
sudo n stable
# update latest npm
sudo npm install npm@latest -g
```

#### Directories
```text
website
├── blog 
│   ├── 2019-05-29-welcome.md
├── docs 
│   ├── doc1.md
│   └── mdx.md
├── src 
│   ├── css 
│   │   └── custom.css
│   └── pages 
│       ├── styles.module.css
│       └── index.js
├── static 
│   └── img
├── versioned_docs
│   └── version-5.0.0
├── versioned_sidebars
│   └── version-5.0.0-sidebars.json
├── docusaurus.config.ts 
├── package.json 
├── README.md
├── sidebars.ts
└── yarn.lock
```



### Preview at local machine

```shell

## Starts the development server.
npm start

## Publishes the website to GitHub pages.
npm run deploy
```

### Website TODO List

- [x] Search in document
- [ ] SEO
- [x] Multi Version
- [ ] i18n

### Tech Article
- [ ] MetadataStore and Job Engine
- [ ] New Frontend and New Modeling Process
- [ ] Index Management
