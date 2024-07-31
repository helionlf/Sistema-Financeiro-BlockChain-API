document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('form_transacao').addEventListener('submit', function (e) {
        e.preventDefault();
        var valor = document.getElementById('valor').value;
        var tipo = document.getElementById('tipo').value;

        fetch('http://localhost:3000/transacoes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ valor: parseFloat(valor), tipo: tipo })
        })
        .then(response => response.json())
        .then(data => {
            console.log('Sucesso:', data);
            var mensagem = document.getElementById('mensagem');
            mensagem.style.color = 'green';
            mensagem.innerHTML = "Transação enviada com sucesso!";
            setTimeout(() => {
                mensagem.innerHTML= "";
            }, 3000);
        })
        .catch((error) => {
            console.error('Erro:', error);
            var mensagem = document.getElementById('mensagem');
            mensagem.style.color = 'red';
            mensagem.innerHTML = "Erro ao enviar transação.";
            setTimeout(() => {
                mensagem.innerHTML= "";
            }, 3000);
        });
    });

    document.getElementById('exibir_transacoes').addEventListener('click', async function () {
        try {
            const response = await fetch('http://localhost:3000/transacoes');
            const data = await response.json();

            const transacoes = data.transacoes;

            if (!Array.isArray(transacoes)) {
                console.error("A resposta não contém um array de transações:", data);
                return;
            }

            var containerTransacoes = document.getElementById('transacoes');
            containerTransacoes.innerHTML = '';

            transacoes.forEach((transacao) => {
                containerTransacoes.innerHTML += `Valor: ${transacao.valor} - ${transacao.tipo}\n`;
            });
        } catch (error) {
            console.error('Erro ao exibir transacoes:', error);
        }
    });

    document.getElementById('limpar_transacoes').addEventListener('click', async function () {
        var containerTransacoes = document.getElementById('transacoes');
            containerTransacoes.innerHTML = '';
    });

    async function exibir_saldo() {
        try {
            const response = await fetch('http://localhost:3000/saldo');
            const data = await response.json();

            const saldo = data.saldo;

            var containerSaldo = document.getElementById('saldo');
            containerSaldo.innerHTML = `<p>Saldo: ${saldo}</p>`;
        } catch (error) {
            console.error('Erro ao exibir saldo:', error);
        }
    }

    function atualizarSaldo() {
        const intervalo = 1000;
        exibir_saldo();
        setInterval(exibir_saldo, intervalo);
    }

    atualizarSaldo();
});
