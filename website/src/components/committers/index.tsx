import React from 'react';
import clsx from 'clsx';

import styles from './styles.module.css';


export type CommitterItem = {
    name: string;
    role: string;
    org: string;
    apacheId: string;
    githubId: string;
    showOnPages: boolean;
}

export default function Committer( {
    name,
    role,
    org,
    apacheId,
    githubId,
    showOnPages,
}: CommitterItem): JSX.Element {
    return (
        <div className={clsx('card', styles.committer)}>
            <div className="card__header">
                <div className="avatar">
                    <img
                        alt={name}
                        className="avatar__photo"
                        src={`https://github.com/${githubId}.png`}
                        width="48"
                        height="48"
                        loading="lazy"
                    />
                    <div className={clsx('avatar__intro', styles.committerMeta)}>
                        <strong className="avatar__name">{name}</strong>
                        <span>{role}</span>
                    </div>
                </div>
            </div>

            <div className="card__footer">
                <p className={clsx(styles.committerMeta)}>
                    {org}
                </p>
            </div>
        </div>

    );
}