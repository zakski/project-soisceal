package alice.tuprolog.interfaces;

import com.szadowsz.gospel.core.data.Struct;
import com.szadowsz.gospel.core.data.Term;
import com.szadowsz.gospel.core.exception.InterpreterError;

public interface ISocketLib {
    boolean tcp_socket_client_open_2(Struct Address, Term Socket) throws InterpreterError;

    boolean tcp_socket_server_open_3(Struct Address, Term Socket, Struct Options) throws InterpreterError;

    boolean tcp_socket_server_accept_3(Term ServerSock, Term Client_Addr, Term Client_Slave_Socket) throws InterpreterError;

    boolean tcp_socket_server_close_1(Term serverSocket) throws InterpreterError;

    boolean read_from_socket_3(Term Socket, Term Msg, Struct Options) throws InterpreterError;

    boolean write_to_socket_2(Term Socket, Term Msg) throws InterpreterError;

    boolean aread_from_socket_2(Term Socket, Struct Options) throws InterpreterError;

    boolean udp_socket_open_2(Struct Address, Term Socket) throws InterpreterError;

    boolean udp_send_3(Term Socket, Term Data, Struct AddressTo) throws InterpreterError;

    boolean udp_receive(Term Socket, Term Data, Struct AddressFrom, Struct Options) throws InterpreterError;

    boolean udp_socket_close_1(Term socket) throws InterpreterError;
}
