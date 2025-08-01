import {AddCustomerResponse, Customer} from "../types.ts";


//const SERVER_URL = 'http://localhost:8081/api/v1/customers'
//const SERVER_URL = '/api/users/api/v1/customers'
const SERVER_URL = '/api/v1/customers'
// MANAGER
const getCustomers = async (filters = {}, page = 0, size = 10, sort:string[]) => {
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
                if (value !== undefined && value !== null && value !== '') {
                    queryParams += `&${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`;
                }
            });

        const url = `${SERVER_URL}?${queryParams}`;
        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch customers');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return;
    }
}

// MANAGER, CUSTOMER
const getCustomer = async (customerId:number) =>{
    try{
        /*if(!customerId) throw new Error('Failed to fetch user');
        const parsedId=parseInt(customerId)*/
        const response = await fetch(`${SERVER_URL}/${customerId}`,{method:'GET'})
        if (!response.ok) throw new Error('Failed to fetch customer');
        return await response.json();
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// TUTTI
const getUserByKeycloakId = async (keycloakId:string) =>{
    try{
        /*if(!customerId) throw new Error('Failed to fetch user');
        const parsedId=parseInt(customerId)*/
        const response = await fetch(`/api/v1/users/keycloak/${keycloakId}`,{method:'GET'})
        console.log(response)

        if (!response.ok) throw new Error('Failed to fetch customer');
        return await response.json();
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// CUSTOMER
const addCustomer = async (customer: Customer, csrf: string ):Promise<AddCustomerResponse> =>{
    try {
        console.log(customer)
        const response = await fetch(SERVER_URL,{
            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(customer),
        })

        if (!response.ok) throw new Error(`Failed to create a new customer`);

        const responseData = await response.json();
        const location = response.headers.get("Location");
        console.log(response)
        console.log(location)

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}


// CUSTOMER
const modifyCustomer = async (customer: Customer, csrf:string) =>{
    try {
        const response = await fetch(`${SERVER_URL}/${customer.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(customer),
        })

        if (!response.ok) throw new Error(`Failed to update the customer`);
        return  response
    }catch(err){
        console.error('Error in the request:', err);
    }


}
// CUSTOMER, MANAGER
const deleteCustomer = async (customerId:number, csrf:string) =>{
    try{
        const response = await fetch(`${SERVER_URL}/${customerId}`,{
            method:'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            }
        })
        if (!response.ok) throw new Error('Failed to delete customer');
        return response/*.json();*/
    }catch(err){
        console.error('Error in the request:', err);
    }
}
// CUSTOMER
const getCustomerEligibility = async (customerId:number) => {
    try {
        const response = await fetch(`${SERVER_URL}/${customerId}/eligibility`, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch customer eligibility');
        const text = await response.text();
        return text.toLowerCase() === 'true';
    } catch (err) {
        console.error('Error in the request:', err);
        return false;
    }
}

const CustomerAPI={
    getCustomers,getCustomer,addCustomer,deleteCustomer,modifyCustomer,getCustomerEligibility,
    getCustomerByKeycloakId: getUserByKeycloakId
}

export default CustomerAPI;