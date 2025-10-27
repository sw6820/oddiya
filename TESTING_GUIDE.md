# Testing Guide - Minimum Unit Testing Standards

## Overview

This document establishes the minimum unit testing standards for all services in the Oddiya project.

## Philosophy

**Minimum Unit Testing = Critical Path Coverage**

We write tests for:
1. ✅ **Business Logic** - Core services and functionality
2. ✅ **Validation** - DTO validation rules
3. ✅ **Configuration** - Critical config classes
4. ✅ **Error Handling** - Exception scenarios
5. ✅ **API Endpoints** - Controller basic behavior

We DON'T write tests for:
- ❌ Simple getters/setters
- ❌ POJOs without logic
- ❌ Trivial DTOs (unless they have validation)
- ❌ Third-party library code

## Test Structure

### Naming Convention

```
{ClassName}Test.java
```

Example:
- `JwtService` → `JwtServiceTest.java`
- `AuthController` → `AuthControllerTest.java`

### Test Method Naming

```java
@Test
void testMethodName_Scenario_ExpectedOutcome() {
    // Given - Arrange
    // When - Act  
    // Then - Assert
}
```

Example:
```java
@Test
void testValidateToken_WithValidToken_ReturnsTrue() {
    // Given
    String token = "valid.jwt.token";
    
    // When
    boolean isValid = jwtService.validateToken(token);
    
    // Then
    assertThat(isValid).isTrue();
}
```

## Test Coverage by Layer

### 1. DTO Tests

**What to test:**
- ✅ Builder pattern works
- ✅ Default values
- ✅ Validation annotations
- ✅ All validation rules

**Example:**
```java
@Test
void testRefreshTokenRequest_WithNullToken_ReturnsValidationError() {
    RefreshTokenRequest request = new RefreshTokenRequest(null);
    
    Set<ConstraintViolation<RefreshTokenRequest>> violations = validator.validate(request);
    
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).contains("required");
}
```

### 2. Service Tests

**What to test:**
- ✅ Main business logic paths
- ✅ Error conditions
- ✅ Edge cases

**Mock external dependencies:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void testRefreshToken_WithInvalidToken_ThrowsException() {
        // Given
        when(redisTemplate.hasKey("refresh_token:invalid")).thenReturn(false);
        
        // When & Then
        assertThrows(InvalidTokenException.class, 
            () -> authService.refreshToken("invalid"));
    }
}
```

### 3. Controller Tests

**What to test:**
- ✅ Endpoints exist and return correct status
- ✅ Request validation
- ✅ Error responses

**Use @WebMvcTest:**
```java
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void testAuthorizeGoogle_ReturnsRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/authorize/google"))
                .andExpect(status().is3xxRedirection());
    }
}
```

### 4. Configuration Tests

**What to test:**
- ✅ Config values are correct
- ✅ Bean creation works
- ✅ Conditional beans

**Example:**
```java
@SpringBootTest
class JwtConfigTest {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Test
    void testJwtConfig_HasValidKeyPair() {
        assertThat(jwtConfig.getKeyPair().getPrivate()).isNotNull();
        assertThat(jwtConfig.getKeyPair().getPublic()).isNotNull();
    }
}
```

## Testing Tools & Libraries

### Java (Spring Boot)

**Required Dependencies:**
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testImplementation 'org.assertj:assertj-core'  // Better assertions
testImplementation 'org.testcontainers:junit-jupiter'  // For integration tests
```

**Annotations:**
- `@SpringBootTest` - Full integration tests
- `@WebMvcTest` - Controller tests
- `@DataJpaTest` - Repository tests
- `@ExtendWith(MockitoExtension.class)` - Service tests with mocking

### Python (FastAPI)

**Required Dependencies:**
```txt
pytest>=7.0.0
pytest-asyncio>=0.21.0
httpx>=0.24.0  # For async testing
```

**Pattern:**
```python
import pytest
from httpx import AsyncClient

@pytest.mark.asyncio
async def test_generate_plan_success():
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.post("/api/v1/generate-plan", json={
            "location": "Seoul",
            "start_date": "2025-01-01",
            "end_date": "2025-01-03"
        })
        assert response.status_code == 200
        assert "daily_plans" in response.json()
```

## Running Tests

### Java

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests JwtServiceTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Python

```bash
# Run all tests
pytest

# Run specific test file
pytest tests/test_llm_service.py

# Run with coverage
pytest --cov=src --cov-report=html
```

## Test Execution in CI

### Pre-Commit Checks

```bash
# Run tests before commit
./gradlew test
pytest

# If any test fails, fix before committing
```

### GitHub Actions Example

```yaml
- name: Run tests
  run: |
    cd services/auth-service
    ./gradlew test
    
- name: Check test coverage
  run: |
    cd services/auth-service
    ./gradlew test jacocoTestReport
```

## Current Test Coverage

### Auth Service ✅
- ✅ Application context loads
- ✅ JWT configuration
- ✅ DTO validation
- ✅ Controller endpoints
- ⏳ Service logic (to be implemented)
- ⏳ Integration tests (to be implemented)

## Best Practices

1. **Keep tests fast** - Unit tests should run in < 1 second each
2. **Use meaningful assertions** - Use AssertJ for better error messages
3. **Mock external dependencies** - Don't call real APIs or databases in unit tests
4. **Test one thing** - Each test should verify one behavior
5. **Use descriptive names** - Test names should describe what's being tested
6. **Test edge cases** - null, empty, boundary conditions
7. **Clean up** - Use `@AfterEach` if needed for test isolation

## Anti-Patterns to Avoid

❌ **Testing implementation details** - Test behavior, not internals  
❌ **Testing third-party code** - Don't test Spring or JWT library code  
❌ **Slow tests** - Avoid database/network calls in unit tests  
❌ **Brittle tests** - Tests that break when refactoring  
❌ **Test everything** - Focus on critical path, not 100% coverage  

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [pytest Documentation](https://docs.pytest.org/)
- [Testcontainers](https://www.testcontainers.org/)

