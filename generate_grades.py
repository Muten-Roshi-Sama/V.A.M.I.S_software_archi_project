import json
import random

# Charger les données
with open('server/src/main/resources/data/students.json', 'r', encoding='utf-8') as f:
    students = json.load(f)

with open('server/src/main/resources/data/annual_study_plans.json', 'r', encoding='utf-8') as f:
    study_plans = json.load(f)

# Types de sessions possibles
sessions = ['LEC', 'LAB', 'PROJ', 'QUIZ']

# Commentaires possibles selon la note
comments = {
    'excellent': ['Excellent travail', 'Remarquable', 'Très impressionnant', 'Performance exceptionnelle'],
    'tres_bien': ['Très bon travail', 'Très bien', 'Solide performance', 'Excellent', 'Très impliqué'],
    'bien': ['Bon travail', 'Bonne compréhension', 'Travail soigné', 'Bonne participation', 'Bon effort'],
    'assez_bien': ['Travail correct', 'Assez bien', 'Progrès réguliers', 'Effort régulier', 'En progression'],
    'passable': ['Peut mieux faire', 'Travail acceptable', 'Doit renforcer', 'À améliorer', 'Juste la moyenne'],
    'insuffisant': ['Insuffisant', 'Doit revoir les bases', 'Beaucoup de lacunes', 'Travail insuffisant', 'Échec']
}

def get_comment(score, max_score):
    """Retourne un commentaire selon la note"""
    percentage = (score / max_score) * 100
    if percentage >= 85:
        return random.choice(comments['excellent'])
    elif percentage >= 75:
        return random.choice(comments['tres_bien'])
    elif percentage >= 65:
        return random.choice(comments['bien'])
    elif percentage >= 55:
        return random.choice(comments['assez_bien'])
    elif percentage >= 50:
        return random.choice(comments['passable'])
    else:
        return random.choice(comments['insuffisant'])

def generate_grade_for_course(student_id, course_code, session):
    """Génère une note aléatoire pour un cours"""
    # Notes entre 8 et 20 avec une distribution plus réaliste
    score = random.choices(
        range(8, 21),
        weights=[1, 2, 3, 5, 7, 10, 12, 15, 12, 10, 8, 6, 3],  # Distribution gaussienne approximative
        k=1
    )[0]
    
    max_score = 20
    
    return {
        "points": score,
        "comment": get_comment(score, max_score),
        "student_id": student_id,
        "activities_id": f"{course_code}-{session}",
        "scale": max_score
    }

# Générer toutes les notes
all_grades = []

for student in students:
    student_id = student.get('studentId')
    study_year = student.get('studyYear')
    option_code = student.get('optionCode')
    
    if not student_id or not study_year:
        continue
    
    # Mapper MECA → EM (variance de nommage)
    if option_code == 'MECA':
        option_code = 'EM'
    
    # Trouver le plan d'études correspondant
    matching_plan = None
    for plan in study_plans:
        if plan['year'] == study_year:
            # Si l'étudiant a une option, chercher le plan correspondant
            if option_code:
                if plan.get('option_code') == option_code:
                    matching_plan = plan
                    break
            # Sinon, prendre le plan sans option (BA1, BA2)
            elif 'option_code' not in plan or plan.get('option_code') is None:
                matching_plan = plan
                break
    
    if not matching_plan:
        print(f"Aucun plan trouvé pour {student_id} (année: {study_year}, option: {option_code})")
        continue
    
    # Générer 2-3 notes par cours
    for course_code in matching_plan['course_list']:
        # Choisir aléatoirement 2 ou 3 sessions par cours
        num_sessions = random.choice([2, 3])
        selected_sessions = random.sample(sessions, num_sessions)
        
        for session in selected_sessions:
            grade = generate_grade_for_course(student_id, course_code, session)
            all_grades.append(grade)

# Sauvegarder dans le fichier grades.json
with open('server/src/main/resources/data/grades.json', 'w', encoding='utf-8') as f:
    json.dump(all_grades, f, indent=2, ensure_ascii=False)

print(f"✅ Généré {len(all_grades)} notes pour {len(students)} étudiants")
print(f"📊 Moyenne: {len(all_grades) // len(students)} notes par étudiant")
