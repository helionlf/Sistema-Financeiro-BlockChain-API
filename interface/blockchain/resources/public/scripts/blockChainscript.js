document.addEventListener('DOMContentLoaded', (event) => {
    document.getElementById('registra_transacoes').addEventListener('click', async function () {
        try {
            const response = await fetch('http://localhost:3000/transacoes');
            const data = await response.json();

            const transacoes = data.transacoes;

            await fetch('http://localhost:3001/transacao', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ transacoes })
            });

            console.log("Todas as suas transacoes foram registradas");

        } catch (error) {
            console.error('Erro ao registrar transacoes:', error);
        }
    });

    document.getElementById('exibir_blockchain').addEventListener('click', async function () {
        try {
            const response = await fetch('http://localhost:3001/chain');
            const blocks = await response.json();

            console.log(blocks)

            var cadeia = document.getElementsByClassName('chain')[0];
            cadeia.innerHTML = ''; // Clear existing content

            blocks.forEach((block) => {
                let blocoHTML = `
                    <div class="block">
                        <div class="id">
                            <p id="id_">ID: ${block.id}</p>
                        </div>
                        <div class="nonce">
                            <p id="nonce">Nonce: ${block.nonce}</p>
                        </div>
                        <div class="dado">
                            <p id="dado">Dado:</p>
                                <textarea 
                                style="width: 580px; max-width: 580px; height: 170px;"
                                name="transacoes" id="transacoes" placeholder="transações registradas" disabled>
                `;

                if (block.id === 1) {
                    blocoHTML += `${block.dados}`;
                } else {
                    blocoHTML += formatarDados(block.dados.transacoes);
                }

                blocoHTML += `
                                </textarea>
                        </div>
                        <div class="hash">
                            <p id="hash">Hash: ${block.hash}</p>
                        </div>
                        <div class="hash_anterior">
                            <p id="hash_anterior">Hash anterior: ${block.hashAnterior}</p>
                        </div>
                    </div>
                `;

                blocoHTML = blocoHTML.replace(/\n\s*/g, "\n");

                cadeia.innerHTML += blocoHTML;
            });

        } catch (error) {
            console.error('Erro ao exibir blockchain:', error);
        }
    });
});

function comoJson(conteudo) {
    const jsonStr = JSON.stringify(conteudo);
    const transacao = JSON.parse(jsonStr);
    return `Valor: ${transacao.valor} - Tipo: ${transacao.tipo}`;
}

function formatarDados(vetor) {
    return vetor.map(transacao => comoJson(transacao)).join("\n");
}





