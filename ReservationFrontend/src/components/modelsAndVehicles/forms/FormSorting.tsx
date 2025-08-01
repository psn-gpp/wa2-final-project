import React, {Dispatch, SetStateAction} from "react";
import {Form} from "react-bootstrap";

interface FormSortingProps {
    //handleSearch: (e: React.FormEvent<HTMLFormElement>) => void;
    sortOptions: string[];
    setSortOptions: Dispatch<SetStateAction<string[]>>;
    sortingParams: string[]
}


const FormSorting: React.FC<FormSortingProps> = ({sortOptions, setSortOptions, sortingParams}) => {

    const handleSortChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const [param, order] = e.target.value.split(",");
        setSortOptions((prev) => {
            // Rimuove la selezione precedente dello stesso parametro
            const filteredSorts = prev.filter((sort) => !sort.startsWith(param));
            console.log([...filteredSorts, `${param},${order}`])
            return [...filteredSorts, `${param},${order}`];
        });
    };

    const handleRemoveSort = (sortToRemove: string) => {
        setSortOptions((prev) => prev.filter((sort) => sort !== sortToRemove));
    };


    return (
        <>
            <div className="mt-3">
                <Form.Label className="fw-bold me-2">Sort by:</Form.Label>
                <Form.Select onChange={handleSortChange} style={{width: "250px"}}>
                    <option value="">Select sorting...</option>
                    {sortingParams?.map((param) => (
                        <>
                            <option key={`${param},asc`} value={`${param},asc`}>
                                {param.charAt(0).toUpperCase() + param.slice(1)} (Asc)
                            </option>
                            <option key={`${param},desc`} value={`${param},desc`}>
                                {param.charAt(0).toUpperCase() + param.slice(1)} (Desc)
                            </option>
                        </>
                    ))}
                </Form.Select>
            </div>

            {/* Badge per mostrare i sort selezionati */}
            <div className="mt-3">
                {sortOptions?.map((sort) => (
                    <span key={sort} className="badge bg-primary me-2">
                                    {sort.replace(",", " - ")}
                        <button type="button" className="btn-close ms-2"
                                onClick={() => handleRemoveSort(sort)}></button>
                                </span>
                ))}
            </div>
        </>

    );

}

export default FormSorting