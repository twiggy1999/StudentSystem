/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.controller;

import java.util.Collection;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.samples.petclinic.model.Student;
import org.springframework.samples.petclinic.repository.StudentRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
class StudentController {

	private static final String VIEWS_CREATE_OR_UPDATE = "students/createOrUpdate";

	private final StudentRepository students;

	public StudentController(StudentRepository stuService) {
		this.students = stuService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/students/new")
	public String initCreationForm(Map<String, Object> model) {
		Student student = new Student();
		model.put("student", student);
		return VIEWS_CREATE_OR_UPDATE;
	}

	@PostMapping("/students/new")
	public String processCreationForm(@Valid Student student, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_CREATE_OR_UPDATE;
		}
		else {
			this.students.save(student);
			return "redirect:/students/" + student.getId();
		}
	}

	@GetMapping("/students/find")
	public String initFindForm(Map<String, Object> model) {
		model.put("student", new Student());
		return "students/findStudents";
	}

	@GetMapping("/students")
	public String processFindForm(Student student, BindingResult result, Map<String, Object> model) {

		// whether has id input
		boolean hasId = false;

		if (student.getId() != null) {
			hasId = true;
		}

		if (student.getName() == null) {
			student.setName(""); // broadest search
		}

		// may be more clear
		if (hasId) {
			// find by id
			Student results = this.students.findById(student.getId());
			if (results == null) {
				// not found
				result.rejectValue("id", "notFound", "not found");
				return "students/findStudents";
			}
			else {
				// student found
				return "redirect:/students/" + student.getId();
			}
		}
		else {
			// find by name
			Collection<Student> results = this.students.findByName(student.getName());
			if (results.isEmpty()) {
				result.rejectValue("name", "notFound", "not found");
				return "students/findStudents";
			}
			else if (results.size() == 1) {
				student = results.iterator().next();
				return "redirect:/students/" + student.getId();
			}
			else {
				model.put("selections", results);
				return "students/studentsList";
			}
		}
	}

	@GetMapping("/students/{studentId}/edit")
	public String initUpdatingForm(@PathVariable("studentId") int studentId, Model model) {
		Student student = this.students.findById(studentId);
		model.addAttribute(student);
		return VIEWS_CREATE_OR_UPDATE;
	}

	@PostMapping("/students/{studentId}/edit")
	public String processUpdatingForm(@Valid Student student, BindingResult result,
			@PathVariable("studentId") int studentId) {
		if (result.hasErrors()) {
			return VIEWS_CREATE_OR_UPDATE;
		}
		else {
			student.setId(studentId);
			this.students.save(student);
			return "redirect:/students/{studentId}";
		}
	}

	@GetMapping("/students/{studentId}/delete")
	public String initDeleteForm(@PathVariable("studentId") int studentId) {
		Student student = this.students.findById(studentId);
		this.students.delete(student.getId());
		return "redirect:/students";
	}

	// display
	@GetMapping("/students/{studentId}")
	public ModelAndView showStudent(@PathVariable("studentId") int studentId) {
		ModelAndView mav = new ModelAndView("students/studentDetails");
		Student student = this.students.findById(studentId);
		mav.addObject(student);
		return mav;
	}

}
