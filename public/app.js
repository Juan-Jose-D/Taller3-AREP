/**
 * Este script maneja la lógica del lado del cliente para interactuar con un servidor que maneja una lista de componentes.
 * Permite agregar componentes a la lista mediante un formulario y mostrar los componentes actuales en una tabla.
 * Se utiliza `fetch` para realizar solicitudes a una API REST.
 *
 * Los componentes tienen los siguientes campos:
 * - name: el nombre del componente.
 * - type: el tipo de componente.
 * - price: el precio del componente.
 */

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("componentForm");
    const componentList = document.getElementById("componentList");
    const componentsHeader = document.getElementById("componentsHeader");
    const componentTable = document.getElementById("componentTable");
    
    // Configuración del sistema de estrellas
    const stars = document.querySelectorAll('.star');
    const ratingInput = document.getElementById('rating');
    
    stars.forEach(star => {
        star.addEventListener('click', () => {
            const value = parseInt(star.getAttribute('data-value'));
            ratingInput.value = value;
            
            // Actualizar la visualización de las estrellas
            stars.forEach((s, index) => {
                if (index < value) {
                    s.classList.add('active');
                } else {
                    s.classList.remove('active');
                }
            });
        });
        
        // Efecto hover
        star.addEventListener('mouseover', () => {
            const value = parseInt(star.getAttribute('data-value'));
            stars.forEach((s, index) => {
                if (index < value) {
                    s.classList.add('hover');
                } else {
                    s.classList.remove('hover');
                }
            });
        });
        
        star.addEventListener('mouseout', () => {
            stars.forEach(s => s.classList.remove('hover'));
        });
    });

    // Cargar los componentes desde el servidor al cargar la página
    fetchComponents();

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const name = document.getElementById("name").value;
        const type = document.getElementById("type").value;
        const description = document.getElementById("description").value;
        const rating = document.getElementById("rating").value;

        if (!name || !type || !description) {
            alert("Todos los campos son obligatorios.");
            return;
        }

        const component = { 
            name, 
            type, 
            description,
            rating: parseInt(rating) || 0 
        };

        try {
            // Enviar una solicitud POST para agregar el componente al servidor
            await fetch("http://localhost:35000/api/components", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(component),
            });

            form.reset();
            // Resetear las estrellas
            stars.forEach(s => s.classList.remove('active'));
            ratingInput.value = "0";
            fetchComponents();
        } catch (error) {
            console.error("Error al agregar el componente:", error);
        }
    });

    async function fetchComponents() {
        try {
            const response = await fetch("http://localhost:35000/api/components");
            const components = await response.json();

            componentList.innerHTML = '';

            if (components.length > 0) {
                componentsHeader.style.display = "block";
                componentTable.style.display = "block";

                components.forEach(({ name, type, description, rating }) => {
                    const row = document.createElement("tr");
                    
                    // Crear representación visual de las estrellas
                    let starsHtml = '';
                    for (let i = 1; i <= 5; i++) {
                        starsHtml += i <= rating ? '★' : '☆';
                    }
                    
                    row.innerHTML = `
                        <td>${name}</td>
                        <td>${type}</td>
                        <td>${description}</td>
                        <td>${starsHtml} (${rating})</td>
                    `;
                    componentList.appendChild(row);
                });
            } else {
                componentsHeader.style.display = "none";
                componentTable.style.display = "none";
            }
        } catch (error) {
            console.error("Error al obtener componentes:", error);
        }
    }
});