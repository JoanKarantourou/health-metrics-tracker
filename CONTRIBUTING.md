# Contributing to Health Metrics Tracker

Thank you for your interest in contributing to Health Metrics Tracker! This document provides guidelines and instructions for contributing to the project.

## Getting Started

1. **Fork** the repository on GitHub
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/your-username/health-metrics-tracker.git
   cd health-metrics-tracker
   ```
3. **Set up** the development environment (see [README.md](README.md) for prerequisites)
4. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Backend (Spring Boot)

```bash
cd backend

# Ensure PostgreSQL is running with a database named health_metrics_db
# Configure credentials in src/main/resources/application.yml

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

### Frontend (React)

```bash
cd frontend/client

# Install dependencies
npm install

# Start the development server
npm start

# Run tests
npm test
```

## Code Style

### Backend (Java)

- Follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- Use meaningful variable and method names
- Keep methods focused and concise
- Add Javadoc comments to public methods
- Use Lombok annotations to reduce boilerplate (`@Getter`, `@Setter`, `@RequiredArgsConstructor`)
- Follow the existing layered architecture: Controller -> Service -> Repository

### Frontend (JavaScript/React)

- Use functional components with hooks
- Follow the existing project structure (pages, components, services, hooks)
- Use Prettier for formatting (configuration in `.prettierrc`)
- Keep components focused on a single responsibility
- Use meaningful component and variable names

## Making Changes

1. **Write tests** for your changes:
   - Backend: JUnit 5 unit tests in `src/test/java/`
   - Frontend: React Testing Library tests alongside components
2. **Follow existing patterns** in the codebase
3. **Keep commits atomic** - each commit should represent a single logical change
4. **Write clear commit messages** following this format:
   ```
   type: short description

   Optional longer description explaining the change.
   ```
   Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

## Submitting Changes

1. **Push** your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
2. **Create a Pull Request** against the `main` branch
3. **Describe your changes** in the PR description:
   - What changes were made and why
   - How to test the changes
   - Any breaking changes or dependencies

## Pull Request Guidelines

- Keep PRs focused and reasonably sized
- Ensure all existing tests pass before submitting
- Add tests for new functionality
- Update documentation if needed
- Respond to review feedback promptly

## Reporting Issues

When reporting bugs, please include:

- A clear description of the issue
- Steps to reproduce the problem
- Expected vs actual behavior
- Your environment (OS, Java version, Node version, browser)
- Relevant error messages or logs

## Project Structure

Refer to [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for a detailed overview of the project architecture and design decisions.

## Questions?

If you have questions about contributing, feel free to open an issue for discussion.

## License

By contributing to this project, you agree that your contributions will be licensed under the [MIT License](LICENSE).
