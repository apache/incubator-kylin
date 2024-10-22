import React from "react";

import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [

    {
        title: <Translate>Ultra Fast Query Experience</Translate>,
        Svg: require('@site/static/img/homepage/fast.svg').default,
        description: (
            <>
                <Translate>Provide sub-second query performance based on advanced pre-calculation technology.</Translate>
                <Translate>Support large-scale, high concurrency data analytics with low hardware and development cost.</Translate>
            </>

        ),
    },
    {
        title: <Translate>Model & Index Recommendation</Translate>,
        Svg: require('@site/static/img/homepage/paperplane.svg').default,
        description: (
            <>
                <Translate>Modeling with SQL text & automatic index optimization based on query history.</Translate>
                <Translate>More intelligent and easier for user to get started.</Translate>
            </>
        ),
    },
    {
        title: <Translate>Internal Table with Native Compute Engine</Translate>,
        Svg: require('@site/static/img/homepage/plugin.svg').default,
        description: (
            <>
                <Translate>More flexible query analysis based on internal table.</Translate>
                <Translate>Integrates Apache Gluten as native compute engine, delivering over a 2x improvement in performance.</Translate>
            </>
        ),
    },
    {
        title: <Translate>Powerful Data Warehouse Capabilities</Translate>,
        Svg: require('@site/static/img/homepage/warehouse.svg').default,
        description: (
            <>
                <Translate>Advanced multi-dimensional analysis, various data functions.</Translate>
                <Translate>Support connecting to different BI tools, like Tableau/Power BI/Excel.</Translate>
            </>
        ),
    },
    {
        title: <Translate>Streaming-Batch Fusion Analysis</Translate>,
        Svg: require('@site/static/img/homepage/streaming.svg').default,
        description: (
            <>
                <Translate>New designed streaming/fusion model capability, reducing data analysis latency to seconds-minutes level.</Translate>
                <Translate>Support fusion analysis with batch data, which brings more accurate and reliable results.</Translate>
            </>
        ),
    },
    {
        title: <Translate>Brand New Web UI</Translate>,
        Svg: require('@site/static/img/homepage/web.svg').default,
        description: (
            <>
                <Translate>The new modeling process is concise, allowing users to define table relationships, dimensions, and measures on a single canvas.</Translate>
            </>
        ),
    },
];

export default FeatureList;