import socket
import threading


'이 파일은 라즈베리파이에서 작업하는 파일입니다.



def handle_client(client_socket):
    try:
        while True:
            message = client_socket.recv(1024).decode('utf-8')
            if not message:
                break
            print(f"Client: {message}")
    except:
        pass
    finally:
        print("다시 연결해주세요")
        client_socket.close()

def start_server():

    server_ip = get_ip_address()
    print(f"Server IP address: {server_ip}")
    server_port = 8888
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((server_ip, server_port))
    server_socket.listen(5)
    print(f"Server started on {server_ip}:{server_port}")

    while True:
        client_socket, addr = server_socket.accept()
        print(f"Client connected: {addr}")
        client_handler = threading.Thread(target=handle_client, args=(client_socket,))
        client_handler.start()


def get_ip_address():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # 연결되지 않은 소켓을 통해 외부 주소로 연결 시도
        s.connect(("8.8.8.8", 80))
        ip_address = s.getsockname()[0]
    except Exception:
        ip_address = "127.0.0.1"
    finally:
        s.close()
    return ip_address


if __name__ == "__main__":
    start_server()