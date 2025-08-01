import {AddReservationResponse, Reservation} from "../types.ts";

//const SERVER_URL = 'http://localhost:8080/api/v1/reservations'
const SERVER_URL = '/api/v1/reservations'

// STAFF e CUSTOMER -> possibilmente da cambiare aggiungendo getReservationsByCustomer
const getReservations = async (filters = {}, page = 0, size = 10, sort:string[]) => {
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
                if (value) {
                    queryParams = `${queryParams}&${key}=${value}`
                }
            })


        const url = `${SERVER_URL}?${queryParams}`;
        console.log(url)

        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch reservations');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return {content: [], totalPages: 0};
    }
}

// CUSTOMER
const addReservation = async (reservation: Reservation, csrf:string):Promise<AddReservationResponse> =>{
    try {
        const response = await fetch(`${SERVER_URL}`,{

            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(reservation),
        })
        console.log(response)
        if (!response.ok) throw new Error(`Failed to create a new reservation`);


        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}

// STAFF e CUSTOMER
const modifyReservation = async (reservation: Reservation, csrf:string) =>{
    try {

        const response = await fetch(`${SERVER_URL}/${reservation.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(reservation),
        })

        if (!response.ok) throw new Error(`Failed to update the reservation`);
        return  response
    }catch(err){
        console.error('Error in the request:', err);
    }


}
// STAFF e CUSTOMER
const deleteReservation = async (reservationId:number|undefined, csrf:string) =>{
    try{
        const response = await fetch(`${SERVER_URL}/${reservationId}`,{
            method:'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            }
        })
        if (!response.ok) throw new Error('Failed to delete reservation');
        return response
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// STAFF e CUSTOMER
const getReservationById = async (reservationId: number | undefined) => {
    console.log("Sono in getReservation By ID !!!!!!!!!!!!")

    try {
        const response = await fetch(`${SERVER_URL}/${reservationId}`, {method: 'GET'});
        if (!response.ok) throw new Error('Failed to fetch reservation');
        const json = await response.json();
        console.log("risposta api: ", json)
        return json
    } catch (err) {
        console.error('Error in the request:', err);
    }
}
// CUSTOMER
const payReservation = async (reservationId: number | undefined, csrf:string) => {
    try {
        const response = await fetch(`${SERVER_URL}/${reservationId}/pay`, {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': csrf
            }
        });

        if (!response.ok) {
            throw new Error('Failed to pay reservation');
        }

        return await response.text();
    } catch (err) {
        console.error('Error in the request:', err);
    }
};

// CUSTOMER
const getPaymentStatus = async (token: string): Promise<{ status: string }> => {
    try {
        const response = await fetch(`${SERVER_URL}/order/${token}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            throw new Error(`Failed to fetch payment status (HTTP ${response.status})`);
        }
        return await response.json() as { status: string };
    } catch (err) {
        console.error('Error fetching payment status:', err);
        throw err;
    }
};

/*
const getVehicleFilters = async () =>{
    const response = await fetch(`${SERVER_URL}/filters`,{method:'GET'})
    if (!response.ok) throw new Error('Failed to fetch vehicle');
    return await response.json();
}
*/

const VehicleAPI = {
    getReservations,
    addReservation,
    modifyReservation,
    deleteReservation,
    getReservationById,
    payReservation,
    getPaymentStatus
}

export default VehicleAPI;