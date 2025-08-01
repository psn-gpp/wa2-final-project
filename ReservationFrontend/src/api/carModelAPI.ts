import {CarModel,AddCarModelResponse} from "../types.ts";

//const SERVER_URL = 'http://localhost:8080/api/v1/models'
//const SERVER_URL = 'http://localhost:8084/api/v1/models'
const SERVER_URL = '/api/v1/models'
// TUTTI
const getCarModels = async (filters = {}, page = 0, size = 10, sort:string[]) => {
    try {

        //translate params for the api request
        //paging
        let queryParams = `page=${page}&size=${size}`

        //sorting
        if(sort.length!=0){
            for(const param of sort){
                queryParams = `${queryParams}&sort=${param}`
            }
        }

        //filtering
        Object.entries(filters)
            .forEach(([key, value]) => {
                if (value /*!= ""*/) {
                    queryParams = `${queryParams}&${key}=${value}`
                }
            })

        const url = `${SERVER_URL}?${queryParams}`;

        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch carModels');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return {content: [], totalPages: 0};
    }
}
// TUTTI
const getCarModel = async (modelId:string|undefined) =>{
    try{
        if(!modelId) throw new Error('Failed to fetch carModel');
        const parsedId=parseInt(modelId)
        const response = await fetch(`${SERVER_URL}/${parsedId}`,{method:'GET'})
        if (!response.ok) throw new Error('Failed to fetch carModel');
        return await response.json();
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// FLEET MANAGER
const addCarModel = async (carModel: CarModel, csrf: string):Promise<AddCarModelResponse> =>{
    try {
        const response = await fetch(SERVER_URL,{
            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(carModel),
        })

        if (!response.ok) throw new Error(`Failed to create a new car model`);

        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}


// FLEET MANAGER
const modifyCarModel = async (carModel: CarModel, csrf:string) =>{
    try {
        const response = await fetch(`${SERVER_URL}/${carModel.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(carModel),
        })

        if (!response.ok) throw new Error(`Failed to update the model`);
        //console.log(response)
        return  response //await response.json(); --> la put non torna il valore aggiornato della entity ??
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }


}
// FLEET MANAGER
const deleteCarModel = async (modelId:string|undefined, csrf:string) =>{
    try{
        if(!modelId) throw new Error('Car model id not valid');
        const parsedId=parseInt(modelId)
        const response = await fetch(`${SERVER_URL}/${parsedId}`,{
            method:'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            },
        })
        if (!response.ok) throw new Error('Failed to delete carModel');
        return response/*.json();*/
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// TUTTI
const getCarModelFilters = async () =>{
    try{
        const response = await fetch(`${SERVER_URL}/filters`,{method:'GET'})
        if (!response.ok) throw new Error('Failed to fetch carModel filters');
        return await response.json();
    }catch(err){
        console.error('Error in the request:', err);
    }

}

const CarModelAPI = {
    getCarModels,
    getCarModel,
    addCarModel,
    modifyCarModel,
    deleteCarModel,
    getCarModelFilters
}

export default CarModelAPI;