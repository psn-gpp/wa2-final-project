import { AddVehicleResponse, Vehicle} from "../types.ts";

//const SERVER_URL = 'http://localhost:8080/api/v1/vehicles'
const SERVER_URL = '/api/v1/vehicles'

// TUTTI LOGIN
const getVehicles = async (filters = {}, page = 0, size = 10, sort:string[]) => {
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
        console.log(url)

        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch vehicles');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return {content: [], totalPages: 0};
    }
}
// TUTTI LOGIN
const getVehicle = async (vehicleId:string|undefined) =>{
    const response = await fetch(`${SERVER_URL}/${vehicleId}`,{method:'GET'})
    if (!response.ok) throw new Error('Failed to fetch vehicle');
    return await response.json();
}
// FLEET MANAGER
const addVehicle = async (vehicle: Vehicle, csrf:string):Promise<AddVehicleResponse> =>{
    try {
        console.log("invio", vehicle)
        const response = await fetch(SERVER_URL,{
            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify({...vehicle,id:null, kilometers: parseFloat(vehicle.kilometers.toString())}),
        })
        console.log(response)
        if (!response.ok) {
            const responseText = await response.text();
            console.log("Errore dettagliato dal backend:", responseText);
            throw new Error(`Failed to create a new vehicle`);
        }

        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}

// FLEET MANAGER
const modifyVehicle = async (vehicle: Vehicle, csrf:string) =>{
    try {
        const response = await fetch(`${SERVER_URL}/${vehicle.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(vehicle),
        })

        if (!response.ok) throw new Error(`Failed to update the vehicle`);
        return  response
    }catch(err){
        console.error('Error in the request:', err);
        //return {data: null, location: null}
    }


}
// FLEET MANAGER
const deleteVehicle = async (vehicleId:number|undefined, csrf:string) =>{
    try{
        /*if(!vehicleId) throw new Error('Vehicle id not valid');
        const parsedId=parseInt(vehicleId)*/
        const response = await fetch(`${SERVER_URL}/${vehicleId}`,{
            method:'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            }
        })
        if (!response.ok) throw new Error('Failed to delete vehicle');
        return response
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// TUTTI LOGIN
const getVehicleFilters = async () =>{
    const response = await fetch(`${SERVER_URL}/filters`,{method:'GET'})
    if (!response.ok) throw new Error('Failed to fetch vehicle');
    return await response.json();
}

const VehicleAPI = {
    getVehicle,
    getVehicles,
    addVehicle,
    modifyVehicle,
    deleteVehicle,
    getVehicleFilters
}

export default VehicleAPI;