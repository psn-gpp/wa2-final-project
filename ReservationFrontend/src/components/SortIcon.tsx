import React from 'react';

type SortIconProps = {
    field: string;
    sort: string[]; // es: ["name,asc", "email,desc"]
};

const SortIcon: React.FC<SortIconProps> = ({ field, sort }) => {
    const sortItem = sort.find(item => item.startsWith(`${field},`));

    if (!sortItem) {
        // Non è ordinato: mostra entrambe le frecce
        return (
            <>
                <i className="bi bi-caret-up"></i>
                <i className="bi bi-caret-down"></i>
            </>
        );
    }

    const direction = sortItem.split(',')[1];
    const iconClass =
        direction === 'asc' ? 'bi-caret-up-fill' : 'bi-caret-down-fill';

    return <i className={`bi ${iconClass}`}></i>;
};

export default SortIcon;