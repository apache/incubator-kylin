import React from 'react';
import clsx from 'clsx';
import USERLOGOS, {type USERITEM} from "../../data/userlogos";
import styles from './styles.module.css';

export default function UsersSection() {
    const userColumns: USERITEM[][] = [[],[],[],[],[],[]];
    USERLOGOS.forEach((user, i) =>
        userColumns[i % 6].push(user),
    );
    return (
        <div>
            <div className="container">
                <div className={clsx('row')}>
                    {userColumns.map((userItems, i) => (
                        <div className="col col--2" key={i}>
                            {userItems.map((userItem, idx) => (
                                <img src={userItem.Svg} alt={userItem.name} className={styles.userLogo}/>
                            ))}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}