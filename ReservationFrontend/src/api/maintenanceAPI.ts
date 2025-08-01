import {AddMaintenanceResponse, Maintenance} from "../types.ts";

//const SERVER_URL = 'http://localhost:8080/api/v1/vehicles'
const SERVER_URL = '/api/v1/vehicles'

// FLET MANAGER e STAFF
const getMaintenances = async (filters = {}, page = 0, size = 10, sort:string[],vehicleId:string|undefined) => {
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

        const url = `${SERVER_URL}/${vehicleId}/maintenances?${queryParams}`;

        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch maintenances');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return {content: [], totalPages: 0};
    }
}
// FLET MANAGER e STAFF
const getMaintenance = async (vehicleId:string|undefined,maintenanceId:string|undefined) =>{
    const response = await fetch(`${SERVER_URL}/${vehicleId}/maintenances/${maintenanceId}`,{method:'GET'})
    if (!response.ok) throw new Error('Failed to fetch maintenance');
    return await response.json();
}
// STAFF
const addMaintenance = async (vehicleId:string|undefined, maintenance: Maintenance, csrf:string):Promise<AddMaintenanceResponse> =>{
    try {
        const response = await fetch(`${SERVER_URL}/${vehicleId}/maintenances`,{
            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(maintenance),
        })

        if (!response.ok) throw new Error(`Failed to create a new maintenance`);

        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}

// FLEET MANAGER e STAFF
const modifyMaintenance = async (vehicleId:string|undefined, maintenance: Maintenance, csrf:string) =>{
    try {
        const response = await fetch(`${SERVER_URL}/${vehicleId}/maintenances/${maintenance.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(maintenance),
        })

        if (!response.ok) throw new Error(`Failed to update maintenance`);
        return  response
    }catch(err){
        console.error('Error in the request:', err);
        //return {data: null, location: null}
    }


}

const MaintenanceAPI = {
    getMaintenances,
    getMaintenance,
    addMaintenance,
    modifyMaintenance
}

export default MaintenanceAPI;