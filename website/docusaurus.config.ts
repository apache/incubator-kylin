// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion
import VersionsArchived from './versionsArchived.json';
import versions from './versions.json';

import {themes, type PrismTheme} from 'prism-react-renderer';

const lightCodeTheme = themes.github
const darkCodeTheme = themes.duotoneDark;

const ArchivedVersionsDropdownItems = Object.entries(VersionsArchived).splice(
    0,
    5,
);

function isPrerelease(version: string) {
  return (
      version.includes('-') ||
      version.includes('alpha') ||
      version.includes('beta') ||
      version.includes('rc')
  );
}

function getLastStableVersion() {
  const lastStableVersion = versions.find((version) => !isPrerelease(version));
  if (!lastStableVersion) {
    throw new Error('unexpected, no stable Docusaurus version?');
  }
  return lastStableVersion;
}
const announcedVersion = getAnnouncedVersion();

function getLastStableVersionTuple(): [string, string, string] {
  const lastStableVersion = getLastStableVersion();
  const parts = lastStableVersion.split('.');
  if (parts.length !== 3) {
    throw new Error(`Unexpected stable version name: ${lastStableVersion}`);
  }
  return [parts[0]!, parts[1]!, parts[2]!];
}

function getAnnouncedVersion() {
  const [major, minor] = getLastStableVersionTuple();
  return `${major}.${minor}`;
}

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Apache Kylin',
  tagline: 'Kylin is a high concurrency, high performance and intelligent OLAP engine that provides low-cost and ultimate data analytics experience  .',
  url: 'https://kylin.apache.org',
  baseUrl: '/latest/',
  onBrokenLinks: 'warn',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'apache', // Usually your GitHub org/user name.
  projectName: 'kylin', // Usually your repo name.
  deploymentBranch:'kylin5_doc',

  // add search plugin

  plugins: [
    [
      require.resolve("@cmfcmf/docusaurus-search-local"),
      {
        // Options here
        // whether to index docs pages
        indexDocs: true,

        // Whether to also index the titles of the parent categories in the sidebar of a doc page.
        // 0 disables this feature.
        // 1 indexes the direct parent category in the sidebar of a doc page
        // 2 indexes up to two nested parent categories of a doc page
        // 3...
        //
        // Do _not_ use Infinity, the value must be a JSON-serializable integer.
        indexDocSidebarParentCategories: 0,

        // whether to index blog pages
        indexBlog: true,

        // whether to index static pages
        // /404.html is never indexed
        indexPages: false,

        // language of your documentation, see next section
        language: "en",

        // setting this to "none" will prevent the default CSS to be included. The default CSS
        // comes from autocomplete-theme-classic, which you can read more about here:
        // https://www.algolia.com/doc/ui-libraries/autocomplete/api-reference/autocomplete-theme-classic/
        // When you want to overwrite CSS variables defined by the default theme, make sure to suffix your
        // overwrites with `!important`, because they might otherwise not be applied as expected. See the
        // following comment for more information: https://github.com/cmfcmf/docusaurus-search-local/issues/107#issuecomment-1119831938.
        style: undefined,

        // The maximum number of search results shown to the user. This does _not_ affect performance of
        // searches, but simply does not display additional search results that have been found.
        maxSearchResults: 8,

        // lunr.js-specific settings
        lunr: {
          // When indexing your documents, their content is split into "tokens".
          // Text entered into the search box is also tokenized.
          // This setting configures the separator used to determine where to split the text into tokens.
          // By default, it splits the text at whitespace and dashes.
          //
          // Note: Does not work for "ja" and "th" languages, since these use a different tokenizer.
          tokenizerSeparator: /[\s\-]+/,
          // https://lunrjs.com/guides/customising.html#similarity-tuning
          //
          // This parameter controls the importance given to the length of a document and its fields. This
          // value must be between 0 and 1, and by default it has a value of 0.75. Reducing this value
          // reduces the effect of different length documents on a term’s importance to that document.
          b: 0.75,
          // This controls how quickly the boost given by a common word reaches saturation. Increasing it
          // will slow down the rate of saturation and lower values result in quicker saturation. The
          // default value is 1.2. If the collection of documents being indexed have high occurrences
          // of words that are not covered by a stop word filter, these words can quickly dominate any
          // similarity calculation. In these cases, this value can be reduced to get more balanced results.
          k1: 1.2,
          // By default, we rank pages where the search term appears in the title higher than pages where
          // the search term appears in just the text. This is done by "boosting" title matches with a
          // higher value than content matches. The concrete boosting behavior can be controlled by changing
          // the following settings.
          titleBoost: 5,
          contentBoost: 1,
          tagsBoost: 3,
          parentCategoriesBoost: 2, // Only used when indexDocSidebarParentCategories > 0
        }
      },
    ],
  ],

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.ts',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/apache/kylin/tree/doc5.0/website/',
          lastVersion: '5.0.0',
          versions: {
            current: {
              label: 'Canary 🚧',
              badge: true,
              path: '/',
              banner: 'none',
            },
            '5.0.0': {
              label: '5.0.0',
              path: '/5.0.0/',
              banner: 'none'
            }
          },
          showLastUpdateAuthor: false,
          showLastUpdateTime: true,
        },
        blog: {
          blogSidebarTitle: 'Technical Blogs',
          blogDescription: 'Technical blogs for Kylin 5.0',
          postsPerPage: 'ALL',
          blogSidebarCount: 20,
          showReadingTime: true,
          readingTime: ({content, frontMatter, defaultReadingTime}) =>
                      defaultReadingTime({content, options: {wordsPerMinute: 100}}),
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl:
            'https://github.com/apache/kylin/tree/doc5.0/website/',
        },
        theme: {
          customCss: ['./src/css/custom.css'],
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Apache Kylin',
        logo: {
          alt: 'Kylin Logo',
          src: 'img/kylin_logo.png',
        },
        items: [
          {
            type: 'docsVersionDropdown',
            position: 'left',
            dropdownActiveClassDisabled: true,
            dropdownItemsAfter: [
              {
                type: 'html',
                value: '<hr class="dropdown-separator">',
              },
              {
                type: 'html',
                className: 'dropdown-archived-versions',
                value: '<b>Archived versions</b>',
              },
              ...ArchivedVersionsDropdownItems.map(
                  ([versionName, versionUrl]) => ({
                    label: versionName,
                    href: versionUrl,
                  }),
              ),
            ]
          },
          {
            type: 'doc',
            docId: 'overview',
            position: 'right',
            label: 'Documentation',
          },
          {
            type: 'doc',
            docId: 'community',
            position: 'right',
            label: 'Community',
          },
          {
            type: 'doc',
            docId: 'development/intro',
            position: 'right',
            label: 'Development',
          },
          {
            type: 'doc',
            docId: 'download',
            position: 'right',
            label: 'Download',
          },
          {
            to: '/blog',
            label: 'Blogs',
            position: 'right'
          },
          {
            href: 'https://github.com/apache/kylin',
            position: 'right',
            className: 'header-github-link',
            'aria-label': 'GitHub repository',
          },
          {
            type: 'search',
            position: 'right',
          }
        ],
      },
      announcementBar: {
        id: `announcementBar-v${announcedVersion}`,
        backgroundColor: '#205d3b',
        textColor: '#FFFFFF',
        content: `⭐️ <b><a target="_blank" href="https://kylin.apache.org/blog/releases/${announcedVersion}">Apache Kylin ${announcedVersion}</a> is released! 🎉🥳️ If you like Kylin, give it a star on <a target="_blank" rel="noopener noreferrer" href="https://github.com/apache/kylin">GitHub</a>!</b> ❤️`,
      },
      footer: {
        logo: {
          alt: 'Apache',
          src: 'img/feather-small.gif',
          href: 'https://apache.org',
          width: 160,
          height: 51,
        },
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Quick Start',
                to: '/docs/quickstart/tutorial',
              },
              {
                label: 'How to contribute',
                to: '/docs/development/how_to_contribute',
              },
              {
                label: 'Roadmap',
                to: 'blog/roadmap_of_kylin_50_cn',
              }
            ]
          },
          {
            title: 'Community',
            items: [
              {
                label: 'JIRA',
                href: 'https://issues.apache.org/jira/projects/KYLIN/issues',
              },
              {
                label: 'Mailing List Archives',
                href: 'https://lists.apache.org/list.html?user@kylin.apache.org',
              },
              {
                label: 'Wiki',
                href: 'https://cwiki.apache.org/confluence/display/KYLIN/',
              },
              {
                label: 'Stack Overflow',
                href: 'https://stackoverflow.com/questions/tagged/kylin',
              }
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Blogs',
                to: '/blog',
              },
              {
                label: 'Source Code',
                href: 'https://github.com/apache/kylin',
              }
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Apache Software Foundation under the terms of the Apache License v2.
        <br>Apache Kylin and its logo are trademarks of the Apache Software Foundation. 
        <br>Built with Docusaurus.`,
      },
      prism: {
        additionalLanguages: ['java'],
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
      docs: {
        sidebar: {
          autoCollapseCategories: true,
        }
      }
    }),
};

export default config;
