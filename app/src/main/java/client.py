import socket
import threading

def receive_messages(client_socket):
    try:
        while True:
            message = client_socket.recv(1024).decode('utf-8')
            if not message:
                break
            print(f"Server: {message}")
    except:
        pass
    finally:
        client_socket.close()

def start_client(server_ip):
    server_port = 8888
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((server_ip, server_port))
    print(f"Connected to server {server_ip}:{server_port}")

    receive_thread = threading.Thread(target=receive_messages, args=(client_socket,))
    receive_thread.start()

    while True:
        message = input("Enter message: ")
        if message:
            client_socket.send(message.encode('utf-8'))

if __name__ == "__main__":
    server_ip = input("Enter server IP address: ")
    start_client(server_ip)