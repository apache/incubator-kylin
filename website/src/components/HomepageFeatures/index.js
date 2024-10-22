import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';
import FeatureList from '@site/src/data/features.tsx'

import Translate, {translate} from '@docusaurus/Translate';

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--4')}>

            <div className={styles.featureContainer}>
                <Svg className={styles.featureSvg} role="img"/>
                <h3 className={styles.featureTitle}>{title}</h3>
            </div>
            <div className="text--center padding-horiz--md">
                <p>{description}</p>
            </div>

        </div>
    );
}

const home_arc = [
    {
        title: "home_arc",
        Svg: require('@site/static/img/homepage/home_arc.svg').default,
    }
]

function  Homearc({Svg, title}) {
    return (
        <div className={clsx('col')}>
            <h1 className="text--center padding-horiz--md"><Translate>Apache Kylin Overview</Translate></h1>
            <Svg className={styles.homearc} role="img"/>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <div>
            <section className={styles.features}>
                <div className="container">
                    <h1 className="text--center padding-horiz--md"><Translate>Key Features</Translate></h1>
                    <div className="row">
                        {FeatureList.map((props, idx) => (
                            <Feature key={idx} {...props} />
                        ))}
                    </div>
                </div>
            </section>
            <section className={styles.homearcs}>
                <div className={clsx(styles.homearc, "container")}>
                    <div className="row">
                        {home_arc.map((props, idx) => (
                            <Homearc key={idx} {...props} />
                        ))}
                    </div>
                </div>
            </section>
        </div>
    );
}
