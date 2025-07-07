console.log("This is Script file")

//timeout for all the alert boxed
setTimeout(() => {
	document.querySelectorAll('.alert').forEach(el =>el.remove());
}, 4000);

// Toggle sidebar visibility and adjust content & toggle icon accordingly
const toggleSidebar = () => {
	
    if($('.sidebar').is(":visible"))
	{
        //true - band karna hai
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left","2%");
        $("#toggle-btn").css("visibility", "visible");
    }
    else
	{
        //false - show karna hai
        $(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
        $("#toggle-btn").css("visibility", "hidden");
    }

};

//On window load: For small screens, hide sidebar and show toggle button
window.addEventListener("load", () =>{
    if (window.innerWidth <= 600) 
	{
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
        $("#toggle-btn").css("visibility", "visible");
    }
	else{
		$("#toggle-btn").css("visibility","hidden");
	}
});

//On document ready: Set initial visibility of toggle button based on sidebar state
$(document).ready(function () {
    // On page load
    if ($('.sidebar').is(":visible"))
	{	
        $("#toggle-btn").css("visibility", "hidden");
    } 
	else
	{
        $("#toggle-btn").css("visibility", "visible");
    }
});

//On window resize: Adjust sidebar and toggle button visibility based on screen size
window.addEventListener("resize", () => {
    if (window.innerWidth <= 600)
	{
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
        $("#toggle-btn").css("visibility", "visible");
    }
	else
	{
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
        $("#toggle-btn").css("visibility", "hidden");
    }
});


// This function handles the live search functionality for contacts.
// It fetches matching contacts from the server based on the input query
// and dynamically updates the search results dropdown.
const search = () => {
    console.log("searching...")

    let query = $("#search-input").val();

    if(query.trim() === "")
	{
        $(".search-result").hide();
    }
	else
	{
        console.log(query);
		
		//sending request to server
		let url = `http://localhost:8282/search/${query}`;
		
		fetch(url)
		.then((response) => {
			return response.json()
		})
		.then((data) => {
			// console.log(data);
            let text =`<div class='list-group'>`

            data.forEach((contact) => {
                text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`;
            });

            text+=`</div>`

            $(".search-result").html(text);
            $(".search-result").show();
		})
		.catch((error) =>{
			console.error("search failed : ",error);
		});
		
        
    }
}


//Forgot password enter email - email validation
function validateEmail(){
	const emailField = document.getElementById("email");
	const email = emailField.value.trim();
	
	const emailRegex = /^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$/;
	
	if(!emailRegex.test(email)){
		alert("Please enter a valid email address.");
		emailField.focus();
		return false;
	}
	
	return true;
}