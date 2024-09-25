import clsx from 'clsx';
import Layout from '@theme/Layout';
import Committers, {type CommitterItem} from "../../data/committers";
import Committer from "../../components/committers";

import Translate, {translate} from '@docusaurus/Translate';
import Heading from "@theme/Heading";
import styles from './styles.module.css';


function CommittersSection() {
    const committerColumns: CommitterItem[][] = [[],[],[]];
    Committers.filter((commiter) => commiter.showOnPages).forEach((committer, i) =>
        committerColumns[i % 3]!.push(committer),
    );

    return (
        <div className={clsx(styles.section, styles.sectionAlt)}>
            <div className="container">
                <div className={clsx('row', styles.committersSection)}>
                    {committerColumns.map((committerItems, i) => (
                        <div className="col col--4" key={i}>
                            {committerItems.map((committer) => (
                                <Committer {...committer} key={committer.apacheId}/>
                            ))}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default function Community(): JSX.Element {
    return (
        <div>
            <main>
                <CommittersSection/>
            </main>
        </div>
    );
}