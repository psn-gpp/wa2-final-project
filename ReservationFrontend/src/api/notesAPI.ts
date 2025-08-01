import {Note} from "../types.ts";

//const SERVER_URL = 'http://localhost:8080/api/v1'
const SERVER_URL = '/api/v1'

// FLET MANAGER e STAFF
const getVehicleNotes = async (
    vehicleId: string,
    page: number = 0,
    size: number = 10,
    sort: string[] = [],
    startDate: string,
    endDate: string,
    author: string
): Promise<{ content: Note[], totalPages: number }> => {
    try {
        // Construct query parameters
        let queryParams = `page=${page}&size=${size}`;

        // Add sorting parameters
        if (sort.length > 0) {
            sort.forEach(param => {
                queryParams += `&sort=${param}`;
            });
        }

        // Add filtering parameters
        if (startDate) queryParams += `&startDate=${startDate}`;
        if (endDate) queryParams += `&endDate=${endDate}`;
        if (author) queryParams += `&author=${author}`;

        const url = `${SERVER_URL}/vehicles/${vehicleId}/notes?${queryParams}`;

        const response = await fetch(url, { method: 'GET' });

        if (!response.ok) throw new Error('Failed to fetch notes');

        return await response.json();
    } catch (err) {
        console.error('Error in the request:', err);
        return { content: [], totalPages: 0 };
    }
};

// FLET MANAGER e STAFF
const addVehicleNote = async (
    vehicleId: string,
    note: Note,
    csrf: string
): Promise<{ location: string | null, data: Note | null }> => {
    try {
        const response = await fetch(`${SERVER_URL}/vehicles/${vehicleId}/notes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },
            body: JSON.stringify(note),
        });

        if (!response.ok) throw new Error('Failed to create a new note');

        const responseData = await response.json();
        const location = response.headers.get("Location");

        return { location, data: responseData };
    } catch (err) {
        console.error('Error in the request:', err);
        return { location: null, data: null };
    }
};
// FLET MANAGER e STAFF
const modifyVehicleNote = async (
    vehicleId: string,
    noteId: string,
    note: Note,
    csrf: string
): Promise<{ data: Note | null }> => {
    try {
        const response = await fetch(`${SERVER_URL}/vehicles/${vehicleId}/notes/${noteId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrf
            },
            body: JSON.stringify(note),
        });

        if (!response.ok) throw new Error('Failed to modify the note');

        const responseData = await response.json();
        return { data: responseData };
    } catch (err) {
        console.error('Error in the request:', err);
        return { data: null };
    }
};
// FLET MANAGER e STAFF
const deleteVehicleNote = async (
    vehicleId: string,
    noteId: string,
    csrf: string
): Promise<{ success: boolean }> => {
    try {
        const response = await fetch(`${SERVER_URL}/vehicles/${vehicleId}/notes/${noteId}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrf
            },
        });

        if (!response.ok) throw new Error('Failed to delete the note');

        return { success: true };
    } catch (err) {
        console.error('Error in the request:', err);
        return { success: false };
    }
};

export const NotesAPI = {
    getVehicleNotes,
    addVehicleNote,
    modifyVehicleNote,
    deleteVehicleNote
};