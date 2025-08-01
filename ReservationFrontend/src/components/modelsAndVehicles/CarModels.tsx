import React, {Dispatch, SetStateAction, useContext, useEffect, useState} from "react";
import CarModelAPI from "../../api/carModelAPI.ts";
import CarGrid from "./CarGrid.tsx";
import { Form, Button, Container, Pagination } from "react-bootstrap";
import {useNavigate} from "react-router-dom";
import CarModelModal from "./modals/CarModelModal.tsx";
import {CarModel, ModelFilters, User} from "../../types.ts";
import FormSorting from "./forms/FormSorting.tsx";
import FormCarModelFilters from "./forms/FormCarModel.tsx";
import {UserContext} from "../../App.tsx";
//import FormFilters from "./FormCarModel.tsx";

interface CarModelsProps {
    showModal: boolean;
    setShowModal: Dispatch<SetStateAction<boolean>>;

}

/*const filterTypes: Record<keyof ModelFilters, 'text' | 'number'> = {
    brand: 'text',
    model: 'text',
    modelYear: 'number',
    segment: 'text',
    doorsNo: 'number',
    seatingCapacity: 'number',
    luggageCapacity: 'number',
    category: 'text'
};*/


const CarModels: React.FC<CarModelsProps> = ({ showModal, setShowModal}) => {
    const navigate = useNavigate();

    const context = useContext(UserContext)

    const user = context.user as User;
    const role = context.role as string[] | null;
    // const userId = context.userId as number;

    console.log("Roles in CarModels:", role);
    // console.log( role?.includes("Manager") || role?.includes("Fleet manager") || role?.includes("Staff") )

    const [carModels, setCarModels] = useState([]);
    const [filters, setFilters] = useState<ModelFilters>({});
    const [page, setPage] = useState(0);
    const [size] = useState(10);
    const [sortOptions, setSortOptions] = useState<string[]>([]);
    const [totalPages, setTotalPages] = useState(0);
    const [engines, setEngines] = useState<string[]>([])
    const [transmissions, setTransimissions] = useState<string[]>([])
    const [drivetrains, setDrivetrains] = useState<string[]>([])
    const [safetyFeatures, setSafetyFeatures] = useState<string[]>([])
    const [infotainemts, setInfotainments] = useState<string[]>([])
    const [categories, setCategories] = useState<string[]>([])




    const sortingParams = [
        "brand", "model", "modelYear", "segment",
        "doorsNo", "seatingCapacity", "luggageCapacity", "category"
    ];

    useEffect(() => {
        fetchCarModels();

        const fetchFilters= async () =>{
            const response = await CarModelAPI.getCarModelFilters()
            setEngines(response.engines)
            setDrivetrains(response.drivetrains)
            setInfotainments(response.infotainments)
            setSafetyFeatures(response.safetyFeatures)
            setTransimissions(response.transmissions)
            setCategories(response.categories)
        }

        fetchFilters()
    }, [page, size]);

    const fetchCarModels = async () => {
        try {
            const response = await CarModelAPI.getCarModels(filters, page, size, sortOptions);
            setCarModels(response.content);
            setTotalPages(response.totalPages);
        } catch (error) {
            console.error("Error fetching car models:", error);
        }
    };

    const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setPage(0); // Reset della paginazione quando si applicano nuovi filtri
        fetchCarModels();
    };


    const handleAddCar = async (newCar:CarModel) => {
        try {
            const { location, data } = await CarModelAPI.addCarModel(newCar, user.csrf as string);

            if (!location) {
                throw new Error("No header in the response");
            }
            // redirect to the new page passing the body of the response
            //redirect and the final URL becomes: http://localhost:8080/ui/http://localhost:8080/api/v1/models/modelId
            //wrong
            //navigate(location.replace(`http://localhost:8080/carModels/${data?.id}`, ""), { state: data });
            navigate(`/ui/carModels/${data?.id}`)

        } catch (error) {
            console.error("Error adding a new car model:", error);
        }
    };

    return (
        <Container className="mt-4">
            <h1 className="mb-4 text-center">Car Models List</h1>

            {/* Filters Form */}
            <Form onSubmit={handleSearch} className="bg-light p-4 rounded shadow-sm mb-4">
                <h4 className="mb-3">Filters</h4>
                <FormCarModelFilters
                    engines={engines}
                    transmissions={transmissions}
                    drivetrains={drivetrains}
                    safetyOptions={safetyFeatures}
                    infotainmentOptions={infotainemts}
                    categories={categories}
                    filters={filters}
                    setFilters={setFilters}
                />
                <FormSorting
                    sortOptions={sortOptions}
                    setSortOptions={setSortOptions}
                    sortingParams={sortingParams}
                />

                <div className="mt-3 text-center">
                    <Button type="submit" variant="primary">
                        Search
                    </Button>
                </div>
            </Form>

            {/* Car Grid */}
            {carModels.length > 0 ? (
                <>
                    <CarGrid carModels={carModels} />

                    {/* Pagination Controls */}
                    <Pagination className="justify-content-center mt-3">
                        <Pagination.Prev
                            onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                            disabled={page === 0}
                        />
                        <Pagination.Item>{`Page ${page + 1} of ${totalPages}`}</Pagination.Item>
                        <Pagination.Next
                            onClick={() => setPage((prev) => (prev < totalPages - 1 ? prev + 1 : prev))}
                            disabled={page >= totalPages - 1}
                        />
                    </Pagination>
                </>
            ) : (
                <p className="text-muted text-center">No car models found</p>
            )}

            {/*form to add a new carmodel*/}
            { role?.includes("Fleet_Manager") ?
                <div className="mb-5">
                    <Button onClick={() => setShowModal(true)}>Add new car model</Button>

                    <CarModelModal
                        show={showModal}
                        handleClose={()=>setShowModal(false)}
                        onSubmitCarModel={handleAddCar}
                        engines={engines}
                        transmissions={transmissions}
                        drivetrains={drivetrains}
                        safetyOptions={safetyFeatures}
                        infotainmentOptions={infotainemts}
                        categories={categories}
                    />
                </div>
            : null }
        </Container>
    );
}

export default CarModels;