# 🏃‍♂️ Practice Games

Um plugin de Minecraft desenvolvido para treinar e aprimorar as habilidades no modo "Party" do servidor Mush. Este plugin oferece uma versão melhorada das corridas mortais, permitindo que os jogadores treinem e batam seus recordes pessoais.

## 🎯 Sobre o Plugin

O **Practice Games** foi criado para suprir a necessidade de treino no modo "Party" do servidor Mush, que estava sendo pouco mantido. O plugin oferece uma experiência de corrida aprimorada com:

- Sistema de recordes pessoais e globais
- Checkpoints personalizados para jogadores VIP
- Múltiplos tipos de corrida
- Suporte a banco de dados SQLite e MySQL
- Interface intuitiva com scoreboards em tempo real

## ✨ Características

### 🏁 Corridas Disponíveis
- **Corrida Mortal** - 2 mapas disponíveis (Grande e END)
- **Corrida Kangaroo** - Versão adaptada do original
- **Corrida Grappler** - Versão adaptada do original

### 🎮 Funcionalidades
- ⏱️ Cronômetro em tempo real na barra de XP
- 📊 Scoreboard lateral com tempos de checkpoint
- 🏆 Sistema de recordes e rankings
- 💾 Suporte a SQLite e MySQL
- 🔒 Checkpoints personalizados para VIPs
- 📈 Estatísticas detalhadas por jogador
- 🔄 Sistema de reinício e cancelamento de corridas

## 📦 Instalação

1. Baixe o arquivo `.jar` do plugin
2. Coloque o arquivo na pasta `plugins` do seu servidor
3. Reinicie o servidor
4. Configure o arquivo `config.yml` gerado
5. Configure as conexões de banco de dados (se for usar MySQL)

## ⚙️ Configuração

### Arquivo config.yml

```yaml
# Configuração do Banco de Dados
MySQL:
  Ativado: false  # true para MySQL, false para SQLite
  Host: "localhost"
  Porta: 3306
  Database: "practicegames"
  Usuario: "root"
  Senha: ""
```

## 🎮 Comandos

### Comandos para Jogadores

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `/minigame iniciar <end|grande|kangaroo|grappler> <checkpoint/spawn>` | Inicia uma corrida | `/minigame iniciar end` |
| `/minigame cancelar` | Cancela a corrida atual | `/minigame cancelar` |
| `/minigame reiniciar` | Reinicia a corrida atual | `/minigame reiniciar` |
| `/minigame top` | Abre o menu de rankings | `/minigame top` |
| `/minigame stats` | Abre suas estatísticas | `/minigame stats` |

### Comandos VIP

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `/minigame criarcheckpoint <mapa> <nome>` | Cria checkpoint personalizado | `/minigame criarcheckpoint end meuspot` |
| `/minigame iniciar <mapa> <checkpoint_personalizado>` | Inicia de checkpoint personalizado | `/minigame iniciar end meuspot` |

### Comandos Administrativos

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `/minigame set <end|grande|kangaroo|grappler> <spawn|saida|checkpoint_name|1-12>` | Define spawn ou checkpoint | `/minigame set grande spawn` |

## 🔐 Permissões

| Permissão | Descrição | Padrão |
|-----------|-----------|--------|
| `partygames.vip` | Permite criar checkpoints personalizados e iniciar de checkpoints | VIP |
| `partygames.set.corrida` | Permite configurar spawns e checkpoints | OP |

## 🏃‍♂️ Tipos de Corrida

### 🔥 Corrida Mortal
A corrida principal do plugin, disponível em dois mapas:

#### Mapa 1 - Grande
- **Checkpoints**: END, DESERTO, JUNGLE, FLORESTA, NETHER
- **Características**: Mapa extenso com diversos biomas
- **Dificuldade**: Média

#### Mapa 2 - END
- **Checkpoints**: END, DESERTO, JUNGLE, FLORESTA, NETHER
- **Características**: Mapa com parkour mais difícil
- **Dificuldade**: Alta

### 🦘 Corrida Kangaroo
- Versão adaptada da corrida original do Mush

### 🪝 Corrida Grappler
- Versão adaptada da corrida original do Mush

## 📍 Sistema de Checkpoints

### Checkpoints Padrão
Todos os mapas possuem checkpoints fixos, que são marcados quando o jogador passa por cima de um bloco especifico:
- **END** (Bloco de Carvão)
- **DESERTO** (Bloco de Ouro)
- **JUNGLE** (Bloco de Esmeralda)
- **FLORESTA** (Bloco de Ferro)
- **NETHER** (Bloco de Redstone)

### Checkpoints Personalizados (VIP)
- Jogadores VIP podem criar checkpoints com nomes personalizados
- Checkpoints são salvos individualmente para cada jogador
- Permite treino específico em seções do mapa
- Nomes podem ter até 20 caracteres (letras, números, \_ e -)

### Tipos de Corrida
- **Corrida Completa**: Inicia do spawn principal, conta para recordes globais
- **Corrida Parcial**: Inicia de um checkpoint, não conta para recordes globais

## 💾 Sistema de Banco de Dados

### SQLite (Padrão)
- Banco local, não requer configuração adicional
- Ideal para servidores pequenos
- Arquivo gerado automaticamente

### MySQL (Opcional)
- Banco externo, permite múltiplos servidores
- Configuração necessária no config.yml
- Ideal para redes de servidores

### Dados Armazenados
- Recordes pessoais por mapa
- Tempos de checkpoint individuais
- Estatísticas de corridas completadas
- Rankings globais

## 🎥 Vídeos Demonstrativos

- **Corrida Mortal - Mapa Grande**: [https://youtu.be/199zJOw-jkw]
- **Corrida Mortal - Mapa END**: [https://youtu.be/hRa4QiwxVdM]

## 🎯 Como Usar

### Iniciando uma Corrida Completa
```
/minigame iniciar <mapa> ou /iniciar <mapa>
Exemplo: /iniciar end
```

### Iniciando de um Checkpoint (VIP)
```
/minigame iniciar <mapa> <checkpoint> ou /iniciar mapa> <checkpoint>
Exemplo: /iniciar grande caverna
```

### Criando um Checkpoint Personalizado (VIP)
```
/minigame criarcheckpoint <mapa> <nome_do_checkpoint>
/minigame iniciar end meulugar
```

### Visualizando Recordes
```
/minigame top ou /top
/minigame stats ou /stats
```

## 🔧 Recursos Técnicos

### Interface do Usuário
- **Cronômetro**: Exibido na barra de XP em tempo real
- **Scoreboard**: Mostra progresso atual e tempos de checkpoint
- **Mensagens**: Feedback detalhado sobre o progresso da corrida

### Sistema de Vida
- Durante a corrida mortal, a vida é limitada a 2 corações
- Morrer durante a corrida resulta em cancelamento automático
- Vida é restaurada ao completar ou cancelar a corrida

### Performance
- Sistema otimizado para múltiplos jogadores simultâneos
- Os jogadores não enxergam outros jogadores quando estão em uma corrida
- Limpeza automática de dados ao sair do servidor
- Logs detalhados para debugging

## 🤝 Contribuição

Contribuições são bem-vindas! Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

---

**Desenvolvido com ❤️ para a comunidade Mush**

*Plugin desenvolvido para aprimorar a experiência de treino no modo Party, oferecendo ferramentas avançadas para jogadores que buscam melhorar suas habilidades nas corridas.*
