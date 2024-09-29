import React from "react";

const FeatureList = [

    {
        title: 'Ultra Fast Query Experience',
        Svg: require('@site/static/img/homepage/fast.svg').default,
        description: (
            <>
                Provide sub-second query performance based on advanced pre-calculation technology.
                Support large-scale, high concurrency data analytics with low hardware and development cost.
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
                More flexible query analysis based on internal table.
                Integrates Apache Gluten as native compute engine, delivering over a 2x improvement in performance .
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
                New designed streaming/fusion model capability, reducing data analysis latency to seconds-minutes level.
                Support fusion analysis with batch data, which brings more accurate and reliable results.
            </>
        ),
    },
    {
        title: 'Brand New Web UI',
        Svg: require('@site/static/img/homepage/web.svg').default,
        description: (
            <>
                The new modeling process is concise, allowing users to define table relationships, dimensions, and measures on a single canvas.
            </>
        ),
    },
];

export default FeatureList;