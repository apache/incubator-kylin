import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [

    {
        title: 'Ultra Fast Query Experience',
        Svg: require('@site/static/img/homepage/fast.svg').default,
        description: (
            <>
				Provide sub-second query performance based on leading pre-calculation technology.
                Support large-scaled, high concurrency data analytics with low hardware and development cost.
            </>

        ),
    },
    {
        title: 'Model & Index Recommendation',
        Svg: require('@site/static/img/homepage/paperplane.svg').default,
        description: (
            <>
                Modeling with SQL text & automatic index optimization based on query history.
                More intelligent and easier for user to get started.
            </>
        ),
    },
    {
        title: 'Internal Table with Native Compute Engine',
        Svg: require('@site/static/img/homepage/plugin.svg').default,
        description: (
            <>
                More flexible query analysis base on internal table.
                Integrate apache gluten as native compute engine, which brings over 2x performance improvement.
            </>
        ),
    },
    {
        title: 'Powerful Data Warehouse Capabilities',
        Svg: require('@site/static/img/homepage/warehouse.svg').default,
        description: (
            <>
                Advanced multi-dimensional analysis, various data functions.
                Support connecting to different BI tools, like Tableau/Power BI/Excel.
            </>
        ),
    },
    {
        title: 'Streaming-Batch Fusion Analysis',
        Svg: require('@site/static/img/homepage/streaming.svg').default,
        description: (
            <>
                New designed streaming/fusion model capability, reduce data analysis latency to seconds-minutes level.
                Support fusion analysis with batch data, which brings more accurate and reliable results.
            </>
        ),
    },
    {
		title: 'Brand New Web UI',
		Svg: require('@site/static/img/homepage/web.svg').default,
		description: (
			<>
				New modeling process are concise by letting user define table
				relationship/dimensions/measures in a single canvas.
			</>
		),
    },
];

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

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <h1 className="text--center padding-horiz--md">Key Features</h1>
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
