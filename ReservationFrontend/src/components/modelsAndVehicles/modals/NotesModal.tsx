import React, {useContext, useEffect, useState} from 'react';
import Modal from 'react-bootstrap/Modal';
import Button from 'react-bootstrap/Button';
import Card from 'react-bootstrap/Card';
import 'bootstrap/dist/css/bootstrap.min.css';
import {Note, NoteFilters, User} from "../../../types.ts";
import {CloseButton, Col, Form, ListGroup, Pagination} from "react-bootstrap";
import FormSorting from "../forms/FormSorting.tsx";
import {NotesAPI} from "../../../api/notesAPI.ts";
import {UserContext} from "../../../App.tsx";

// Props per il componente modal
interface NoteDetailsModalProps {
    vehicleId: number,
    isOpen: boolean;
    onClose: () => void;
}

const NotesModal: React.FC<NoteDetailsModalProps> = ({vehicleId, isOpen, onClose}) => {
    const context = useContext(UserContext) ;

    const user = context.user as User;

    const defaultNote: Note = {
        id: 0,
        vehicleId: vehicleId,
        author: '',
        text: '',
        date: '',
    }

    const [notes, setNotes] = useState<Note[]>([]);
    const [filters, setFilters] = useState<NoteFilters>({
        author: "",
        startDate: "",
        endDate: "",
    });
    const [page, setPage] = useState(0);
    const [size] = useState(10); // number of notes per page
    const [sortOptions, setSortOptions] = useState<string[]>([]);
    const [totalPages, setTotalPages] = useState(0);

    const [selectedNote, setSelectedNote] = useState<Note>(defaultNote); // Supponiamo che tu abbia una nota selezionata
    const [editNote, setEditNote] = useState(false);
    const [addNote, setAddNote] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);


    useEffect(() => {
        const fetchNotes = async () => {
            try {
                const response = await NotesAPI.getVehicleNotes(
                    vehicleId.toString(),
                    page,
                    size,
                    sortOptions,
                    filters.startDate,
                    filters.endDate,
                    filters.author
                )
                setNotes(response.content);
                setTotalPages(response.totalPages)
                console.log(response);
            } catch (e) {
                console.error("ERROR fetch notes: ", e)
            }
        }

        fetchNotes();
    }, [page, filters])

    useEffect(()=>{},[notes])


    console.log(notes)


    // Formatta la data per la visualizzazione
    const formattedDate = (date: string) => new Date(date).toLocaleDateString('en-CA');


    const handleAdd = async () => {
        setAddNote(true)
    }

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        try {
            console.log("dentro submit")
            if (addNote && !editNote) {
                console.log("dentro add")
                const {location, data} = await NotesAPI.addVehicleNote(
                    vehicleId.toString(),
                    selectedNote,
                    user.csrf as string
                )
                if(!location || !data ){
                    throw new Error("Error in the response");
                }
                setNotes((prev) => [...prev,data]);

            } else if (editNote && !addNote) {
                console.log("dentro edit")
                await NotesAPI.modifyVehicleNote(
                    vehicleId.toString(),
                    selectedNote.id.toString(),
                    selectedNote,
                    user.csrf as string
                )
                setNotes((prev) =>
                    prev.map((n) => n.id === selectedNote.id ? selectedNote : n)
                );
            }
            setEditNote(false)
            setAddNote(false)
        } catch (e) {
            console.error("ERROR submit note: ", e)
        }
    }

    const handleShowDeleteModal = async () => {
        setShowDeleteModal(true)
    }

    const handleDelete = async () => {
        try {
            const response = await NotesAPI.deleteVehicleNote(
                vehicleId.toString(),
                selectedNote.id.toString(),
                user.csrf as string
            )
            setNotes((prev)=>prev.filter((n)=>n.id!=selectedNote.id))
            console.log(response)
        } catch (e) {
            console.error("ERROR delete note: ", e)
        }
    }

    const handleEdit = async () => {
        setEditNote(true)
    }


    return (
        <>
            <Modal show={isOpen} onHide={onClose} centered size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>Notes</Modal.Title>
                </Modal.Header>

                <Modal.Body>

                    <div className="mb-3 d-flex justify-content-around ">
                        <Col md={7} className="mt-4">
                            <strong>Filters</strong>
                            <Col className="d-flex gap-3 align-items-center">
                                Author:
                                <Form.Control
                                    type="text"
                                    title="author"
                                    value={filters.author}
                                    onChange={
                                        (e) =>
                                            setFilters(
                                                (prev: NoteFilters) => {
                                                    return {
                                                        author: e.target.value,
                                                        startDate: prev.startDate,
                                                        endDate: prev.endDate
                                                    }
                                                }
                                            )
                                    }
                                ></Form.Control>
                            </Col>
                            <Col className="d-flex gap-3 align-items-center">
                                Date:
                                <Form.Control
                                    type="date"
                                    value={filters.startDate}
                                    title="start date"
                                    onChange={
                                        (e) =>
                                            setFilters(
                                                (prev: NoteFilters) => {
                                                    return {
                                                        author: prev.author,
                                                        startDate: e.target.value,
                                                        endDate: prev.endDate
                                                    }
                                                }
                                            )
                                    }
                                />
                                -
                                <Form.Control
                                    type="date"
                                    value={filters.endDate}
                                    title="end date"
                                    onChange={
                                        (e) =>
                                            setFilters(
                                                (prev: NoteFilters) => {
                                                    return {
                                                        author: prev.author,
                                                        startDate: prev.startDate,
                                                        endDate: e.target.value
                                                    }
                                                }
                                            )
                                    }
                                />
                            </Col>
                        </Col>
                        <Col md={4}>
                            {/*<strong>Sort By</strong>
                <FormSelect>
                    <option></option>
                </FormSelect>*/}
                            <FormSorting
                                sortOptions={sortOptions}
                                setSortOptions={setSortOptions}
                                sortingParams={["Date", "Author"]}
                            />
                        </Col>
                    </div>
                    <div className="d-flex">
                        <Col md={6}>

                            <Card className="mb-3 p-0" style={{height: '100%'}}>
                                {selectedNote.id != 0 && !editNote && !addNote ?
                                    <>
                                        <CloseButton
                                            style={{position: "absolute", right: "10px", top: "10px"}}
                                            onClick={() => setSelectedNote(defaultNote)}
                                        ></CloseButton>
                                        <Card.Body className="m-3">
                                            <Card.Text>
                                                <strong>Note ID:</strong> {selectedNote.id}
                                            </Card.Text>
                                            <Card.Text>
                                                <strong>Vehicle ID:</strong> {selectedNote.vehicleId}
                                            </Card.Text>
                                            <Card.Text>
                                                <strong>Author:</strong> {selectedNote.author}
                                            </Card.Text>
                                            <Card.Text>
                                                <strong>Date:</strong> {formattedDate(selectedNote.date)}
                                            </Card.Text>
                                            <Card.Text>
                                                <strong>Text:</strong> {selectedNote.text}
                                            </Card.Text>
                                        </Card.Body>
                                        <Card.Footer className="d-flex justify-content-end gap-2">
                                            <Button variant="outline-danger" onClick={() => {
                                                handleShowDeleteModal()
                                            }}>
                                                Delete
                                            </Button>
                                            <Button variant="warning" onClick={() => {
                                                handleEdit()
                                            }}>
                                                Edit
                                            </Button>
                                        </Card.Footer>
                                    </>
                                    :

                                    <label hidden={addNote} className="text-center pt-3" style={{color: "#bbbbbb"}}>
                                        No note selected
                                    </label>

                                }
                                {(addNote || (selectedNote.id != 0 && editNote)) &&
                                    <Form onSubmit={handleSubmit}>
                                        <Card.Header>
                                            <CloseButton
                                                style={{position: "absolute", right: "10px", top: "10px"}}
                                                onClick={() => {
                                                    setSelectedNote(defaultNote);
                                                    setAddNote(false);
                                                    setEditNote(false)
                                                }
                                                }></CloseButton>
                                            <div className="d-flex align-items-center gap-3">
                                                <strong>Note ID:</strong>{selectedNote.id}
                                                <strong>Vehicle ID:</strong>{selectedNote.vehicleId}
                                            </div>
                                        </Card.Header>
                                        <Card.Body>
                                            <div className="d-flex align-items-start gap-3">
                                                <Card.Text>
                                                    <strong>Author:</strong>
                                                </Card.Text>
                                                <Form.Control
                                                    type="text"
                                                    value={selectedNote.author}
                                                    onChange={(e) =>
                                                        setSelectedNote((note) => {
                                                                return {
                                                                    id: note.id,
                                                                    vehicleId: note.vehicleId,
                                                                    text: note.text,
                                                                    date: formattedDate(note.date),
                                                                    author: e.target.value
                                                                }
                                                            }
                                                        )
                                                    }
                                                ></Form.Control>
                                            </div>
                                            <div className="d-flex align-items-start gap-3">
                                                <Card.Text>
                                                    <strong>Date:</strong>
                                                </Card.Text>
                                                <Form.Control
                                                    type="date"
                                                    value={formattedDate(selectedNote.date)}
                                                    onChange={(e) =>
                                                        setSelectedNote((note) => {
                                                                return {
                                                                    id: note.id,
                                                                    vehicleId: note.vehicleId,
                                                                    text: note.text,
                                                                    date: e.target.value,
                                                                    author: note.author
                                                                }
                                                            }
                                                        )
                                                    }
                                                ></Form.Control>
                                            </div>
                                            <div className="d-flex align-items-start gap-3">
                                                <Card.Text>
                                                    <strong>Text:</strong>
                                                </Card.Text>
                                                <Form.Control
                                                    type="textarea"
                                                    style={{height: '100px'}}
                                                    value={selectedNote.text}
                                                    onChange={(e) =>
                                                        setSelectedNote((note) => {
                                                                return {
                                                                    id: note.id,
                                                                    vehicleId: note.vehicleId,
                                                                    text: e.target.value,
                                                                    date: formattedDate(note.date),
                                                                    author: note.author
                                                                }
                                                            }
                                                        )
                                                    }
                                                ></Form.Control>
                                            </div>
                                        </Card.Body>
                                        <Card.Footer>
                                            <Button type="submit" variant="success">
                                                Confirm
                                            </Button>
                                        </Card.Footer>
                                    </Form>
                                }
                            </Card>
                        </Col>
                        <Col md={6}>
                            <Card style={{height: '100%'}} className="p-2">
                                <ListGroup variant="flush"
                                           style={{minHeight: '250px', maxHeight: '250px', overflowY: 'auto'}}>
                                    {notes.map((nota: Note) => (

                                        <ListGroup.Item
                                            variant="light"
                                            active={selectedNote.id == nota.id}
                                            onClick={() => setSelectedNote(selectedNote.id != 0? defaultNote : nota)}
                                            onMouseOver={(e) => {
                                                e.currentTarget.style.cursor = 'pointer';
                                            }}
                                        >
                                            <strong>{nota.text}</strong><br/>
                                            <small>{nota.author}</small><br/>
                                            <small>{nota.date}</small>
                                        </ListGroup.Item>
                                    ))}
                                </ListGroup>
                                <Pagination className="justify-content-center mt-3 mb-0">
                                    <Pagination.Prev
                                        onClick={() => {
                                            setPage((prev) => Math.max(prev - 1, 0))
                                        }}
                                        disabled={page === 0}
                                    />
                                    <Pagination.Item>{`Page ${page + 1} of ${totalPages}`}</Pagination.Item>
                                    <Pagination.Next
                                        onClick={() => {
                                            setPage((prev) => (prev < totalPages - 1 ? prev + 1 : prev))
                                        }}
                                        disabled={page >= totalPages - 1}
                                    />
                                </Pagination>
                            </Card>
                        </Col>
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button title="Add New Note" variant="primary" onClick={() => {
                        handleAdd()
                    }}>
                        Add New Note
                    </Button>
                </Modal.Footer>
            </Modal>

            <Modal show={showDeleteModal}>
                <Modal.Header closeButton>
                    <Modal.Title>Delete Note</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    Are you sure you want to delete this note?
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="danger" onClick={() => {
                        handleDelete()
                    }}>
                        Delete
                    </Button>
                </Modal.Footer>
            </Modal>
        </>
    )
        ;
};

export default NotesModal;