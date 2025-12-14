# 🏆 REGRA DE OURO - METODOLOGIA DO PATRICK

## ⚠️ ANTES DE COMMITAR QUALQUER IMPLEMENTAÇÃO, VERIFIQUE ESTE CHECKLIST

---

## 📋 CHECKLIST OBRIGATÓRIO

### ✅ 1. MODEL (Entidade)

**Padrão do Patrick:**
- [ ] Usa `@Entity` (javax.persistence, não jakarta)
- [ ] Usa `@Id` com `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- [ ] Usa `@Column(name = "nome_exato_do_banco")` em TODOS os campos
- [ ] Nome da classe em PORTUGUÊS (ex: Usuario, não User)
- [ ] Campos privados com tipos corretos (Long para INTEGER, String para VARCHAR)
- [ ] Construtor vazio obrigatório
- [ ] Construtor com parâmetros (opcional)
- [ ] Getters e Setters para TODOS os campos
- [ ] Nomes dos métodos: `getIdUsuario()`, `setIdUsuario()` (camelCase)

**Exemplo correto:**
```java
@Entity
public class Usuario {
    @Id
    @Column(name = "id_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    
    @Column(name = "nome")
    private String name;
    
    // Construtor vazio
    public Usuario() {}
    
    // Getters e Setters
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
```

**❌ NUNCA FAÇA:**
- Usar `jakarta.persistence` (Patrick usa `javax.persistence`)
- Esquecer `@Column(name = "...")` 
- Usar nomes em inglês se a tabela está em português
- Criar campos sem getters/setters

---

### ✅ 2. DAO (Data Access Object)

**Padrão do Patrick:**
- [ ] Classe no pacote `org.example.DAO`
- [ ] Nome: `[Entidade]DAO` (ex: `AssinaturaDAO`, `ContaDAO`)
- [ ] Usa `ConnectionFactory.getConnection()` para obter conexão
- [ ] SEMPRE usa `try-with-resources` (try com parênteses)
- [ ] SEMPRE usa `PreparedStatement` (nunca Statement puro)
- [ ] SQL usa `?` para parâmetros (nunca concatenação de strings)
- [ ] Preenche parâmetros com `stmt.setString()`, `stmt.setInt()`, etc.
- [ ] Para INSERT/UPDATE/DELETE: usa `executeUpdate()`
- [ ] Para SELECT: usa `executeQuery()` e processa `ResultSet`
- [ ] Trata `SQLException` com try-catch
- [ ] Imprime erro: `System.out.println("Erro: " + e.getMessage())`
- [ ] Métodos retornam objetos ou void (nunca ResultSet)

**Estrutura obrigatória:**
```java
public class AssinaturaDAO {
    
    public void salvar(Assinatura assinatura) {
        String sql = "INSERT INTO assinatura(id_usuario, id_plano, status, data_fim) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, assinatura.getIdUsuario());
            stmt.setInt(2, assinatura.getIdPlano());
            stmt.setString(3, assinatura.getStatus());
            stmt.setDate(4, java.sql.Date.valueOf(assinatura.getDataFim()));
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Erro ao salvar assinatura: " + e.getMessage());
        }
    }
    
    public Assinatura buscarPorId(Integer id) {
        String sql = "SELECT * FROM assinatura WHERE id_assin = ?";
        Assinatura assinatura = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                assinatura = new Assinatura();
                assinatura.setIdAssin(rs.getInt("id_assin"));
                assinatura.setIdUsuario(rs.getInt("id_usuario"));
                assinatura.setIdPlano(rs.getInt("id_plano"));
                assinatura.setStatus(rs.getString("status"));
                assinatura.setDataFim(rs.getDate("data_fim").toLocalDate());
            }
            
        } catch (SQLException e) {
            System.out.println("Erro ao buscar assinatura: " + e.getMessage());
        }
        
        return assinatura;
    }
}
```

**❌ NUNCA FAÇA:**
- Usar `Statement` em vez de `PreparedStatement`
- Concatenar strings no SQL: `"SELECT * FROM usuario WHERE id = " + id` ❌
- Esquecer `try-with-resources`
- Não fechar conexões manualmente (deixa try-with-resources fazer)
- Retornar `ResultSet` diretamente
- Usar `jakarta.persistence` no DAO

---

### ✅ 3. CONNECTION FACTORY

**Padrão do Patrick:**
- [ ] Classe no pacote `org.example.Persistence`
- [ ] Nome: `ConnectionFactory`
- [ ] Constantes: `URL`, `USER`, `PASS` (private static final)
- [ ] Método: `public static Connection getConnection()`
- [ ] Retorna `Connection` ou `null` em caso de erro
- [ ] Trata `SQLException`
- [ ] Imprime erro: `System.out.println("Erro ao conectar ao banco: " + e.getMessage())`

**Estrutura obrigatória:**
```java
public class ConnectionFactory {
    private static final String URL = "jdbc:postgresql://host:port/database";
    private static final String USER = "usuario";
    private static final String PASS = "senha";
    
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco: " + e.getMessage());
            return null;
        }
    }
}
```

**❌ NUNCA FAÇA:**
- Criar múltiplas factories
- Hardcode de credenciais em outros lugares
- Não tratar exceções

---

### ✅ 4. SERVICE (Lógica de Negócio)

**Padrão do Patrick:**
- [ ] Classe no pacote `org.example.Service`
- [ ] Nome: `[Entidade]Service` (ex: `AssinaturaService`)
- [ ] Cria instância do DAO: `private [Entidade]DAO dao = new [Entidade]DAO()`
- [ ] Métodos públicos que chamam o DAO
- [ ] Pode adicionar validações antes de chamar DAO

**Estrutura obrigatória:**
```java
public class AssinaturaService {
    private AssinaturaDAO dao = new AssinaturaDAO();
    
    public void criarAssinatura(Assinatura assinatura) {
        // Validações (opcional)
        if (assinatura.getIdUsuario() == null) {
            throw new RuntimeException("ID do usuário obrigatório");
        }
        
        // Chama DAO
        dao.salvar(assinatura);
    }
}
```

---

### ✅ 5. NOMENCLATURA

**Padrão do Patrick:**
- [ ] Classes Model: PORTUGUÊS (Usuario, Assinatura, Conta)
- [ ] Classes DAO: PORTUGUÊS + DAO (UsuarioDAO, AssinaturaDAO)
- [ ] Classes Service: PORTUGUÊS + Service (UsuarioService)
- [ ] Métodos: camelCase em português (salvar, buscarPorId, listarTodos)
- [ ] Variáveis: camelCase (usuario, assinatura, conn, stmt, rs)

**Exemplos corretos:**
- ✅ `UsuarioDAO.salvar(Usuario usuario)`
- ✅ `AssinaturaDAO.buscarPorId(Integer id)`
- ✅ `ConnectionFactory.getConnection()`

**Exemplos errados:**
- ❌ `UserDAO.save(User user)` (inglês)
- ❌ `AssinaturaDAO.findById()` (inglês)
- ❌ `ConnectionFactory.getConn()` (abreviação)

---

### ✅ 6. IMPORTS

**Padrão do Patrick:**
- [ ] Model: `javax.persistence.*` (NÃO jakarta)
- [ ] DAO: `java.sql.*` + `org.example.Model.*` + `org.example.Persistence.*`
- [ ] Service: `org.example.DAO.*` + `org.example.Model.*`

**Imports corretos para Model:**
```java
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
```

**Imports corretos para DAO:**
```java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.Model.Assinatura;
import org.example.Persistence.ConnectionFactory;
```

---

### ✅ 7. TRATAMENTO DE DADOS

**Conversões obrigatórias:**
- [ ] `LocalDate` → `java.sql.Date.valueOf(localDate)` para salvar
- [ ] `rs.getDate("coluna").toLocalDate()` para ler
- [ ] `Integer` → `rs.getInt()` ou `stmt.setInt()`
- [ ] `Long` → `rs.getLong()` ou `stmt.setLong()`
- [ ] `String` → `rs.getString()` ou `stmt.setString()`
- [ ] `BigDecimal/NUMERIC` → `rs.getBigDecimal()` ou `stmt.setBigDecimal()`

---

### ✅ 8. ESTRUTURA DE MÉTODOS DAO

**Métodos obrigatórios (CRUD completo):**
- [ ] `salvar([Entidade] entidade)` - CREATE
- [ ] `buscarPorId(Integer/Long id)` - READ (um)
- [ ] `listarTodos()` - READ (todos)
- [ ] `atualizar([Entidade] entidade)` - UPDATE
- [ ] `deletar(Integer/Long id)` - DELETE

**Métodos opcionais:**
- [ ] `buscarPor[Campo](tipo valor)` - busca customizada
- [ ] `existePor[Campo](tipo valor)` - verificação

---

### ✅ 9. VERIFICAÇÃO FINAL ANTES DE COMMITAR

**Checklist final:**
- [ ] Todos os métodos seguem o padrão try-with-resources
- [ ] Todos usam PreparedStatement (não Statement)
- [ ] Todos os SQL usam `?` (não concatenação)
- [ ] Todos os campos do Model têm `@Column(name = "...")`
- [ ] Todos os Models usam `javax.persistence` (não jakarta)
- [ ] Todos os DAOs usam `ConnectionFactory.getConnection()`
- [ ] Nomes em português (classes, métodos, variáveis)
- [ ] Getters e Setters para todos os campos
- [ ] Tratamento de exceções em todos os métodos
- [ ] Código compila sem erros

---

## 🔍 COMPARAÇÃO COM O CÓDIGO DO PATRICK

**Sempre compare seu código com:**
1. `UserDAO.java` - padrão de DAO
2. `Usuario.java` - padrão de Model
3. `ConnectionFactory.java` - padrão de conexão

**Perguntas para fazer:**
- Meu código tem a mesma estrutura?
- Uso os mesmos imports?
- Uso os mesmos padrões de nomenclatura?
- Trato erros da mesma forma?
- Uso try-with-resources?
- Uso PreparedStatement?

---

## 📝 TEMPLATE PARA NOVO DAO

Copie e adapte:

```java
package org.example.DAO;

import org.example.Model.[Entidade];
import org.example.Persistence.ConnectionFactory;
import java.sql.*;

public class [Entidade]DAO {
    
    public void salvar([Entidade] entidade) {
        String sql = "INSERT INTO [tabela]([campos]) VALUES ([?])";
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // stmt.set[Tipo](posição, valor);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Erro ao salvar [entidade]: " + e.getMessage());
        }
    }
    
    public [Entidade] buscarPorId(Integer id) {
        String sql = "SELECT * FROM [tabela] WHERE [id_coluna] = ?";
        [Entidade] entidade = null;
        
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                entidade = new [Entidade]();
                // entidade.set[Campo](rs.get[Tipo]("[coluna"]));
            }
            
        } catch (SQLException e) {
            System.out.println("Erro ao buscar [entidade]: " + e.getMessage());
        }
        
        return entidade;
    }
}
```

---

## ⚠️ REGRA DE OURO FINAL

**ANTES DE QUALQUER COMMIT:**
1. Leia este checklist completo
2. Compare seu código com o do Patrick
3. Verifique cada item do checklist
4. Se algo estiver diferente, CORRIJA antes de commitar
5. Só commite quando estiver 100% alinhado com a metodologia

**Lembre-se:** Consistência é mais importante que velocidade. 
Seguir o padrão do Patrick garante que todo o código seja uniforme e fácil de entender.

---

**Última atualização:** 2025-12-03
**Baseado em:** Código do Patrick na branch `origin/patrick`

