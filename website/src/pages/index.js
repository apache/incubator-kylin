import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import styles from './index.module.css';

import Translate, {translate} from '@docusaurus/Translate';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
        <header className={clsx(styles.heroBanner)}>
            <div className={styles.container}>
                <h1 className={styles.title}><Translate>Smarter and Faster</Translate></h1>
                <p className={styles.subtitle}><Translate>{siteConfig.tagline}</Translate></p>
                <br/>
                <div>
                  <Link className={clsx("button button--lg button--secondary", styles.buttons)}
                    to="/docs/overview">
                      <Translate>What's New</Translate>
                  </Link>
                    <span>&emsp;&emsp;&emsp;</span>
                  <Link className={clsx("button button--lg button--secondary", styles.buttons1)}
                    to="/docs/quickstart/intro">
                      <Translate>Play in Docker</Translate>
                    </Link>
                </div>
            </div>
        </header>
  );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`Welcome to ${siteConfig.title}`}
            description="Description will go into a meta tag in <head />">
            <HomepageHeader/>
            <main>
                <div>
                    <HomepageFeatures/>
                </div>
            </main>
        </Layout>
    );
}
