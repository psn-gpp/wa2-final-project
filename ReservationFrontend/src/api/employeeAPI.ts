import {AddEmployeeResponse, Employee} from "../types.ts";


//const SERVER_URL = 'http://localhost:8081/api/v1/employees'
//const SERVER_URL = '/api/users/api/v1/employees'
const SERVER_URL = '/api/v1/employees'
// MANAGER
const getEmployees = async (filters = {}, page = 0, size = 10, sort:string[]) => {
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
        console.log(url)

        const response = await fetch(url, {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch users');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return;
    }
}
/*
const getUserTypes = async () => {
    try {
        const response = await fetch('http://localhost:8080/api/v1/userTypes', {method: 'GET'});

        if (!response.ok) throw new Error('Failed to fetch users');
        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return;
    }
}*/
// MANAGER, STAFF, FLEET MANAGER
const getEmployee = async (employeeId:number) =>{
    try{
        /*if(!customerId) throw new Error('Failed to fetch user');
        const parsedId=parseInt(customerId)*/
        const response = await fetch(`${SERVER_URL}/${employeeId}`,{method:'GET'})
        if (!response.ok) throw new Error('Failed to fetch user');
        return await response.json();
    }catch(err){
        console.error('Error in the request:', err);
    }

}
// MANAGER
const addEmployee = async (employee: Employee, csrf:string):Promise<AddEmployeeResponse> =>{
    try {
        console.log("add", employee)

        const response = await fetch(SERVER_URL,{
            method:'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(employee),
        })

        if (!response.ok) throw new Error(`Failed to create a new employee`);

        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    }catch(err){
        console.error('Error in the request:', err);
        return {data: null, location: null}
    }

}


// MANAGER, STAFF, FLEET MANAGER
const modifyEmployee = async (employee: Employee, csrf:string) =>{
    try {
        const response = await fetch(`${SERVER_URL}/${employee.id}`,{
            method:'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },

            body: JSON.stringify(employee),
        })

        if (!response.ok) throw new Error(`Failed to update the model`);
        return  response
    }catch(err){
        console.error('Error in the request:', err);
    }


}
// MANAGER
const deleteEmployee = async (employeeId:number, csrf:string) =>{
    try{
        const response = await fetch(`${SERVER_URL}/${employeeId}`,{
            method:'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            },
        })
        if (!response.ok) throw new Error('Failed to delete carModel');
        return response
    }catch(err){
        console.error('Error in the request:', err);
    }
}

const EmployeeAPI={
    getEmployees,
    getEmployee,
    modifyEmployee,
    addEmployee,
    deleteEmployee
}

export default EmployeeAPI;