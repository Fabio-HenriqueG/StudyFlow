<?php
/**
 * Endpoint Seguro em PHP para Comunicação com a API do Google Gemini AI
 */

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../config/env.php';

// Garante que a requisição seja POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Método não permitido. Use POST.']);
    exit;
}

// Lê o corpo do JSON enviado pelo cliente
$input = file_get_contents('php://input');
$data = json_decode($input, true);

$prompt = isset($data['prompt']) ? trim($data['prompt']) : '';

if (empty($prompt)) {
    http_response_code(400);
    echo json_encode(['error' => 'O campo prompt é obrigatório.']);
    exit;
}

// System Instruction para orientar o modelo
$systemInstruction = "Você é o Assistente Virtual do StudyFlow, um tutor educacional inteligente, motivador e focado em estudantes. Ajude com resumos, dicas de organização de estudos, explicação de conceitos e criação de roteiros de revisão. Responda em português de forma clara, amigável e direta.";

// Monta o payload para o Google Gemini API
$payload = [
    'contents' => [
        [
            'parts' => [
                ['text' => $systemInstruction . "\n\nPergunta do Estudante: " . $prompt]
            ]
        ]
    ]
];

// Se nenhuma chave foi fornecida ainda, simula uma resposta de tutor inteligente
if (GEMINI_API_KEY === 'SUA_GEMINI_API_KEY_AQUI') {
    $respostaSimulada = "Olá! Eu sou o Assistente IA do StudyFlow. Para me conectar ao cérebro do Gemini na nuvem, adicione sua chave gratuita do Google AI Studio no arquivo `config/env.php`.\n\nEnquanto isso, aqui vai uma dica de estudo: Use a técnica Pomodoro (25 min de foco + 5 min de descanso) para manter a concentração alta!";
    echo json_encode(['response' => $respostaSimulada]);
    exit;
}

// Configura a requisição cURL para a API do Gemini
$url = GEMINI_API_URL . '?key=' . GEMINI_API_KEY;

$ch = curl_init($url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
curl_setopt($ch, CURLOPT_TIMEOUT, 30);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$error = curl_error($ch);
curl_close($ch);

if ($error) {
    http_response_code(500);
    echo json_encode(['error' => 'Falha na comunicação com o servidor de IA: ' . $error]);
    exit;
}

$responseData = json_decode($response, true);

if ($httpCode === 200 && isset($responseData['candidates'][0]['content']['parts'][0]['text'])) {
    $text = $responseData['candidates'][0]['content']['parts'][0]['text'];
    echo json_encode(['response' => $text]);
} else {
    $errorMessage = isset($responseData['error']['message']) ? $responseData['error']['message'] : 'Resposta inválida do serviço de IA.';
    http_response_code($httpCode ?: 500);
    echo json_encode(['error' => $errorMessage]);
}
