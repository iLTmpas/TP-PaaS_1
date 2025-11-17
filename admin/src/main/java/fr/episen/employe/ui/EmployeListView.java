package fr.episen.employe.ui;

import fr.episen.base.ui.component.ViewToolbar;
import fr.episen.employe.EmployeEntity;
import fr.episen.employe.EmployeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("employes")
@PageTitle("Gestion des Employés")
@Menu(order = 1, icon = "vaadin:users", title = "Employés")
class EmployeListView extends Main {

    private final EmployeService employeService;
    private final Grid<EmployeEntity> employeGrid;

    EmployeListView(EmployeService employeService) {
        this.employeService = employeService;

        Button addBtn = new Button("Ajouter un employé", new Icon(VaadinIcon.PLUS), event -> openCreateDialog());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        employeGrid = new Grid<>();
        employeGrid.setItems(query -> employeService.list(toSpringPageRequest(query)).stream());

        employeGrid.addColumn(EmployeEntity::getNom).setHeader("Nom").setSortable(true);
        employeGrid.addColumn(EmployeEntity::getPrenom).setHeader("Prénom").setSortable(true);
        employeGrid.addColumn(EmployeEntity::getMail).setHeader("Email").setSortable(true);
        employeGrid.addColumn(employe -> employe.getBadgeId().toString()).setHeader("Badge ID");
        employeGrid.addComponentColumn(employe -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(employe.getValide());
            checkbox.setReadOnly(true);
            return checkbox;
        }).setHeader("Valide");

        employeGrid.addComponentColumn(employe -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openEditDialog(employe));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> openDeleteDialog(employe));

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Actions");

        employeGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Gestion des Employés", ViewToolbar.group(addBtn)));
        add(employeGrid);
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ajouter un employé");

        TextField nomField = new TextField("Nom");
        nomField.setRequired(true);
        nomField.setMaxLength(50);

        TextField prenomField = new TextField("Prénom");
        prenomField.setRequired(true);
        prenomField.setMaxLength(50);

        EmailField mailField = new EmailField("Email");
        mailField.setRequired(true);
        mailField.setMaxLength(100);

        Checkbox valideField = new Checkbox("Valide");

        FormLayout formLayout = new FormLayout(nomField, prenomField, mailField, valideField);
        dialog.add(formLayout);

        Button saveBtn = new Button("Enregistrer", event -> {
            try {
                if (nomField.isEmpty() || prenomField.isEmpty() || mailField.isEmpty()) {
                    Notification.show("Veuillez remplir tous les champs obligatoires", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                employeService.createEmploye(nomField.getValue(), prenomField.getValue(),
                        mailField.getValue(), valideField.getValue());
                employeGrid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show("Employé ajouté avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openEditDialog(EmployeEntity employe) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifier l'employé");

        TextField nomField = new TextField("Nom");
        nomField.setValue(employe.getNom());
        nomField.setRequired(true);
        nomField.setMaxLength(50);

        TextField prenomField = new TextField("Prénom");
        prenomField.setValue(employe.getPrenom());
        prenomField.setRequired(true);
        prenomField.setMaxLength(50);

        EmailField mailField = new EmailField("Email");
        mailField.setValue(employe.getMail());
        mailField.setRequired(true);
        mailField.setMaxLength(100);

        Checkbox valideField = new Checkbox("Valide");
        valideField.setValue(employe.getValide());

        FormLayout formLayout = new FormLayout(nomField, prenomField, mailField, valideField);
        dialog.add(formLayout);

        Button saveBtn = new Button("Enregistrer", event -> {
            try {
                if (nomField.isEmpty() || prenomField.isEmpty() || mailField.isEmpty()) {
                    Notification.show("Veuillez remplir tous les champs obligatoires", 3000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                employeService.updateEmploye(employe.getIdEmploye(), nomField.getValue(),
                        prenomField.getValue(), mailField.getValue(), valideField.getValue());
                employeGrid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show("Employé modifié avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openDeleteDialog(EmployeEntity employe) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Confirmer la suppression");
        dialog.add("Êtes-vous sûr de vouloir supprimer l'employé " + employe.getPrenom() + " " + employe.getNom() + " ?");

        Button deleteBtn = new Button("Supprimer", event -> {
            try {
                employeService.deleteEmploye(employe.getIdEmploye());
                employeGrid.getDataProvider().refreshAll();
                dialog.close();
                Notification.show("Employé supprimé avec succès", 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage(), 3000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelBtn = new Button("Annuler", event -> dialog.close());

        dialog.getFooter().add(cancelBtn, deleteBtn);
        dialog.open();
    }
}
