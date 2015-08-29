package alice.tuprolog.interfaces;


import alice.tuprolog.core.data.Struct;
import alice.tuprolog.core.data.Term;
import alice.tuprolog.core.exception.PrologException;

public interface ISocketLib {
    public boolean tcp_socket_client_open_2(Struct Address, Term Socket) throws PrologException;
    
    public boolean tcp_socket_server_open_3(Struct Address, Term Socket, Struct Options) throws PrologException; 
    
    public boolean tcp_socket_server_accept_3(Term ServerSock, Term Client_Addr, Term Client_Slave_Socket) throws PrologException;
    
    public boolean tcp_socket_server_close_1(Term serverSocket) throws PrologException;
    
    public boolean read_from_socket_3(Term Socket, Term Msg, Struct Options) throws PrologException;
    
    public boolean write_to_socket_2(Term Socket, Term Msg) throws PrologException;
    
    public boolean aread_from_socket_2(Term Socket, Struct Options) throws PrologException;
    
    public boolean udp_socket_open_2(Struct Address, Term Socket) throws PrologException;
    
    boolean udp_send_3(Term Socket, Term Data, Struct AddressTo) throws PrologException;
    
    boolean udp_receive(Term Socket, Term Data, Struct AddressFrom, Struct Options) throws PrologException;

    public boolean udp_socket_close_1(Term socket) throws PrologException;
}
